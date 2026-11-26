package it.luce.robomow.protocol

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RobomowProtocolTest {

    private fun robotPacket(type: Int, payload: ByteArray): ByteArray {
        val p = ByteArray(5 + payload.size)
        p[0] = 0xAA.toByte(); p[1] = p.size.toByte(); p[2] = 0x1E; p[3] = type.toByte()
        payload.copyInto(p, 4)
        p[p.size - 1] = RobomowProtocol.checksum(p, p.size - 1).toByte()
        return p
    }

    @Test
    fun checksumMatchesKnownKeepAlive() {
        // Pacchetto "nessuna operazione" da RobomowRemoteControl (RS615u): AA 0A 1F 1A 00 00 00 00 00 chk
        val payload = ByteArray(5)
        val frame = RobomowProtocol.frame(0x1A, payload)
        assertEquals("AA 0A 1F 1A 00 00 00 00 00 12", frame.toHex())
    }

    @Test
    fun getConfigFrame() {
        assertEquals("AA 05 1F 0F 22", RobomowProtocol.getConfig().toHex())
    }

    @Test
    fun rsStateRequestFrame() {
        val f = RobomowProtocol.rsStateRequest(0x0102)
        assertEquals(9, f.size)
        assertEquals("AA 09 1F 16 01 02 00 0B", f.copyOf(8).toHex())
        assertEquals(RobomowProtocol.checksum(f, 8), f[8].toInt() and 0xFF)
    }

    @Test
    fun autoOperationFrame() {
        val f = RobomowProtocol.autoOperation(5, RobomowProtocol.OperationMode.HOME)
        assertEquals("AA 09 1F 15 00 05 03 FF", f.copyOf(8).toHex())
    }

    @Test
    fun authPayloadIsPaddedTo15() {
        val p = RobomowProtocol.authPayload("6411800003071")
        assertEquals(15, p.size)
        assertEquals(0, p[13].toInt())
        assertEquals('6'.code, p[0].toInt())
        assertTrue(RobomowProtocol.isAuthOk(ByteArray(15) { 1 }))
        assertFalse(RobomowProtocol.isAuthOk(ByteArray(15) { if (it == 3) 0 else 1 }))
    }

    @Test
    fun parserHandlesFragmentsGarbageAndBadChecksum() {
        val good = robotPacket(0x0F, byteArrayOf(1, 2, 0, 7, 3))
        val bad = good.clone().also { it[it.size - 1] = (it[it.size - 1] + 1).toByte() }
        val stream = byteArrayOf(0x00, 0x11) + bad + good
        val garbage = mutableListOf<String>()
        val parser = PacketParser { _, why -> garbage += why }

        val out = mutableListOf<Packet>()
        stream.toList().chunked(3).forEach { out += parser.feed(it.toByteArray()) }

        assertEquals(1, out.size)
        assertArrayEquals(good, out[0].raw)
        assertTrue(garbage.contains("checksum errato"))
    }

    @Test
    fun parsesConfig() {
        val pkt = PacketParser().feed(robotPacket(0x0F, byteArrayOf(1, 6, 0x00, 0x2A, 4))).single()
        val cfg = MowerConfig.parse(pkt.payload)
        assertNotNull(cfg)
        assertEquals(MowerFamily.RS, cfg!!.family)
        assertEquals("6.42", cfg.softwareLabel)
        assertEquals(4, cfg.mainboardVersion)
    }

    @Test
    fun parsesRsState() {
        // contatore 0007, sottotipo 000B, flag 0x21 (carica + lama), modo 2, batteria 0x80|76, partenza 90 min, durata FFFF
        val payload = byteArrayOf(0, 7, 0, 0x0B, 0x21, 0x02, (0x80 or 76).toByte(), 0, 90, 0xFF.toByte(), 0xFF.toByte())
        val pkt = PacketParser().feed(robotPacket(0x16, payload)).single()
        val s = RsState.parse(pkt)
        assertNotNull(s)
        assertEquals(76, s!!.batteryPercent)
        assertTrue(s.antiTheftActive)
        assertTrue(s.isCharging)
        assertTrue(s.bladeMotorActive)
        assertEquals("Taglio", s.systemModeLabel)
        assertEquals(90, s.minutesToNextDeparture)
        assertNull(s.autoOperationMinutes)
    }

    @Test
    fun hexRoundTrip() {
        assertEquals("AA 05 1F 0F", parseHex("aa:05-1f 0F").toHex())
    }
}
