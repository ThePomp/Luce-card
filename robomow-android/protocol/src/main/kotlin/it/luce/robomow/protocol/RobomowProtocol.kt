package it.luce.robomow.protocol

/**
 * Protocollo BLE dei Robomow (serie RS/RC/RX/RT).
 *
 * Fonti (tutte ricostruite dalla comunita', nessuna documentazione ufficiale):
 *  - collegamento e autenticazione: comuni a tutti i modelli Bluetooth
 *    (arjanmels/robomow_ble, lutskov/RobomowRemoteControl su RS615u);
 *  - stato e comandi della serie RS: TheCutter/RoboMow2MQTT, ricavati dalle
 *    classi RobotDataMiscellaneousRs / RbleAutomaticOperation dell'app ufficiale.
 *
 * Le parti marcate "DA VERIFICARE" vanno confermate col log Bluetooth (HCI snoop)
 * catturato dall'app ufficiale su un RS625.
 *
 * Formato di un pacchetto:
 *   [0] 0xAA  inizio
 *   [1] lunghezza totale (inclusi inizio e checksum)
 *   [2] 0x1F se inviato al robot, 0x1E se ricevuto dal robot
 *   [3] tipo di messaggio
 *   [4..n-2] dati
 *   [n-1] checksum = NOT(somma dei byte precedenti) & 0xFF
 */
object RobomowProtocol {
    const val UUID_SERVICE = "ff00a501-d020-913c-1234-56d97200a6a6"
    const val UUID_CHAR_AUTH = "ff00a502-d020-913c-1234-56d97200a6a6"
    /** Dati verso il robot (write). */
    const val UUID_CHAR_DATA_OUT = "ff00a503-d020-913c-1234-56d97200a6a6"
    /** Dati dal robot (notify). */
    const val UUID_CHAR_DATA_IN = "ff00a506-d020-913c-1234-56d97200a6a6"
    const val UUID_CCCD = "00002902-0000-1000-8000-00805f9b34fb"

    const val START_BYTE = 0xAA
    const val DIR_TO_ROBOT = 0x1F
    const val DIR_FROM_ROBOT = 0x1E
    const val AUTH_LENGTH = 15

    /** Tipi di messaggio noti. */
    object Type {
        const val ACKNOWLEDGE = 0x04
        const val CLEAR_USER_MESSAGE = 0x0E
        const val GET_CONFIG = 0x0F
        const val AUTO_OPERATION = 0x15
        const val MISCELLANEOUS = 0x16
        const val MANUAL_DRIVE = 0x1A
        const val GET_MESSAGE = 0x1B
        const val UPDATE_DATE_TIME = 0x1D
        const val WRITE_EEPROM = 0x1F
        const val READ_EEPROM = 0x20

        fun name(type: Int): String = when (type) {
            ACKNOWLEDGE -> "ACK"
            CLEAR_USER_MESSAGE -> "CLEAR_MSG"
            GET_CONFIG -> "GET_CONFIG"
            AUTO_OPERATION -> "AUTO_OP"
            MISCELLANEOUS -> "MISC"
            MANUAL_DRIVE -> "MANUAL"
            GET_MESSAGE -> "GET_MESSAGE"
            UPDATE_DATE_TIME -> "DATE_TIME"
            WRITE_EEPROM -> "WRITE_EEPROM"
            READ_EEPROM -> "READ_EEPROM"
            else -> "0x%02X".format(type)
        }
    }

    /** Sottotipi del messaggio MISCELLANEOUS. */
    object Misc {
        /** Stato del robot sulla serie RS. */
        const val RS_ROBOT_STATE = 0x000B
    }

    /** Modalita' di funzionamento automatico (LSOperationModes). DA VERIFICARE su RS. */
    enum class OperationMode(val code: Int, val label: String) {
        STOP(0, "Stop"),
        EDGE(1, "Solo bordo"),
        MOW(2, "Taglio"),
        HOME(3, "Torna alla base"),
    }

    /** Zona "principale / tutte" nei comandi di taglio. */
    const val ZONE_MAIN = 0xFF

    fun checksum(data: ByteArray, length: Int = data.size): Int {
        var sum = 0
        for (i in 0 until length) sum += data[i].toInt() and 0xFF
        return sum.inv() and 0xFF
    }

    /** Costruisce un pacchetto completo verso il robot. */
    fun frame(type: Int, payload: ByteArray = ByteArray(0)): ByteArray {
        val packet = ByteArray(5 + payload.size)
        packet[0] = START_BYTE.toByte()
        packet[1] = packet.size.toByte()
        packet[2] = DIR_TO_ROBOT.toByte()
        packet[3] = type.toByte()
        payload.copyInto(packet, 4)
        packet[packet.size - 1] = checksum(packet, packet.size - 1).toByte()
        return packet
    }

    /** Numero di serie della scheda madre, ASCII, completato a 15 byte con zeri. */
    fun authPayload(mainboardSerial: String): ByteArray {
        val ascii = mainboardSerial.trim().toByteArray(Charsets.US_ASCII)
        require(ascii.isNotEmpty()) { "Numero di serie vuoto" }
        require(ascii.size <= AUTH_LENGTH) { "Numero di serie troppo lungo (max $AUTH_LENGTH caratteri)" }
        return ascii.copyOf(AUTH_LENGTH)
    }

    /** Il robot risponde con 15 byte 0x01 se l'autenticazione e' riuscita. */
    fun isAuthOk(response: ByteArray?): Boolean =
        response != null && response.isNotEmpty() && response.all { it.toInt() == 0x01 }

    // --- Messaggi -------------------------------------------------------------------------

    /** Chiede famiglia, versione software e versione scheda madre. Valido per tutti i modelli. */
    fun getConfig(): ByteArray = frame(Type.GET_CONFIG)

    /** Richiesta di stato della serie RS (MISCELLANEOUS, sottotipo 0x0B). */
    fun rsStateRequest(counter: Int): ByteArray =
        frame(Type.MISCELLANEOUS, u16(counter) + u16(Misc.RS_ROBOT_STATE))

    /** Avvio/stop/base. Formato preso dalla serie RC, DA VERIFICARE su RS. */
    fun autoOperation(counter: Int, mode: OperationMode, zone: Int = ZONE_MAIN): ByteArray =
        frame(Type.AUTO_OPERATION, u16(counter) + byteArrayOf(mode.code.toByte(), zone.toByte()))

    private fun u16(value: Int): ByteArray =
        byteArrayOf(((value shr 8) and 0xFF).toByte(), (value and 0xFF).toByte())
}

/** Pacchetto ricevuto e gia' validato. */
class Packet(val raw: ByteArray) {
    val type: Int get() = raw[3].toInt() and 0xFF
    val payload: ByteArray get() = raw.copyOfRange(4, raw.size - 1)
    val typeName: String get() = RobomowProtocol.Type.name(type)
}

/**
 * Ricompone i pacchetti dalle notifiche BLE, che possono arrivare spezzate o unite.
 * I byte non validi vengono scartati e segnalati tramite [onGarbage].
 */
class PacketParser(
    private val onGarbage: (ByteArray, String) -> Unit = { _, _ -> },
) {
    private val buffer = ArrayList<Byte>()

    fun feed(data: ByteArray): List<Packet> {
        data.forEach { buffer.add(it) }
        val out = mutableListOf<Packet>()
        while (buffer.size >= 4) {
            val start = buffer[0].toInt() and 0xFF
            val dir = buffer[2].toInt() and 0xFF
            val len = buffer[1].toInt() and 0xFF
            if (start != RobomowProtocol.START_BYTE || dir != RobomowProtocol.DIR_FROM_ROBOT || len < 5) {
                onGarbage(byteArrayOf(buffer.removeAt(0)), "byte fuori sequenza")
                continue
            }
            if (buffer.size < len) break
            val packet = ByteArray(len) { buffer[it] }
            repeat(len) { buffer.removeAt(0) }
            if (RobomowProtocol.checksum(packet, len - 1) != (packet[len - 1].toInt() and 0xFF)) {
                onGarbage(packet, "checksum errato")
                continue
            }
            out += Packet(packet)
        }
        return out
    }

    fun reset() = buffer.clear()
}

enum class MowerFamily(val code: Int) {
    UNKNOWN(0), RS(1), RC(2), RX(3), RK(4), RT(5);

    companion object {
        fun from(code: Int) = entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}

/** Risposta a GET_CONFIG: famiglia, versione software, release, versione scheda madre. */
data class MowerConfig(
    val family: MowerFamily,
    val familyCode: Int,
    val softwareVersion: Int,
    val softwareRelease: Int,
    val mainboardVersion: Int,
) {
    val softwareLabel: String get() = "$softwareVersion.$softwareRelease"

    companion object {
        fun parse(payload: ByteArray): MowerConfig? {
            if (payload.size < 5) return null
            val f = payload[0].toInt() and 0xFF
            return MowerConfig(
                family = MowerFamily.from(f),
                familyCode = f,
                softwareVersion = payload[1].toInt() and 0xFF,
                softwareRelease = ((payload[2].toInt() and 0xFF) shl 8) or (payload[3].toInt() and 0xFF),
                mainboardVersion = payload[4].toInt() and 0xFF,
            )
        }
    }
}

/**
 * Stato della serie RS (risposta MISCELLANEOUS sottotipo 0x0B).
 *
 * Payload (dopo il tipo di messaggio):
 *   [0-1] contatore   [2-3] sottotipo = 0x000B
 *   [4] flag: bit0-1 sorgente di carica, bit5 motore lama attivo
 *   [5] stato operativo: bit0-2 modalita' di sistema
 *   [6] batteria: bit0-6 percentuale, bit7 antifurto attivo
 *   [7-8] minuti alla prossima partenza   [9-10] durata operazione automatica
 *
 * Interpretazione DA VERIFICARE su RS625: per questo i byte grezzi restano disponibili.
 */
data class RsState(
    val flags: Int,
    val operationalState: Int,
    val batteryRaw: Int,
    val minutesToNextDeparture: Int?,
    val autoOperationMinutes: Int?,
    val raw: ByteArray,
) {
    val batteryPercent: Int get() = batteryRaw and 0x7F
    val antiTheftActive: Boolean get() = (batteryRaw and 0x80) != 0
    val chargeSource: Int get() = flags and 0x03
    val isCharging: Boolean get() = chargeSource != 0
    val bladeMotorActive: Boolean get() = (flags and 0x20) != 0
    val systemMode: Int get() = operationalState and 0x07

    val systemModeLabel: String
        get() = when (systemMode) {
            0 -> "Fermo"
            1 -> "Bordo"
            2 -> "Taglio"
            3 -> "Verso la base"
            else -> "Modalita' $systemMode"
        }

    override fun equals(other: Any?) = other is RsState && raw.contentEquals(other.raw)
    override fun hashCode() = raw.contentHashCode()

    companion object {
        fun parse(packet: Packet): RsState? {
            if (packet.type != RobomowProtocol.Type.MISCELLANEOUS) return null
            val p = packet.payload
            if (p.size < 7) return null
            val sub = ((p[2].toInt() and 0xFF) shl 8) or (p[3].toInt() and 0xFF)
            if (sub != RobomowProtocol.Misc.RS_ROBOT_STATE) return null
            fun u16(i: Int): Int? =
                if (p.size > i + 1) ((p[i].toInt() and 0xFF) shl 8) or (p[i + 1].toInt() and 0xFF) else null
            return RsState(
                flags = p[4].toInt() and 0xFF,
                operationalState = p[5].toInt() and 0xFF,
                batteryRaw = p[6].toInt() and 0xFF,
                minutesToNextDeparture = u16(7)?.takeIf { it != 0xFFFF },
                autoOperationMinutes = u16(9)?.takeIf { it != 0xFFFF },
                raw = packet.raw,
            )
        }
    }
}

fun ByteArray.toHex(): String = joinToString(" ") { "%02X".format(it) }

/** Accetta "AA 05 1F 0F", "aa051f0f", "AA:05:..." */
fun parseHex(text: String): ByteArray {
    val clean = text.replace(Regex("[^0-9A-Fa-f]"), "")
    require(clean.isNotEmpty() && clean.length % 2 == 0) { "Esadecimale non valido" }
    return ByteArray(clean.length / 2) { clean.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
