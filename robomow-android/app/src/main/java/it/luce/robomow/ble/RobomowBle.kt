package it.luce.robomow.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import it.luce.robomow.protocol.MowerConfig
import it.luce.robomow.protocol.MowerFamily
import it.luce.robomow.protocol.Packet
import it.luce.robomow.protocol.PacketParser
import it.luce.robomow.protocol.RobomowProtocol
import it.luce.robomow.protocol.RobomowProtocol.OperationMode
import it.luce.robomow.protocol.RsState
import it.luce.robomow.protocol.toHex
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.util.UUID

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data object Authenticating : ConnectionState
    data object Connected : ConnectionState
    data class Failed(val reason: String) : ConnectionState
}

data class ScannedDevice(
    val address: String,
    val name: String?,
    val rssi: Int,
    /** true se il dispositivo pubblicizza il servizio Robomow. */
    val isRobomow: Boolean,
)

enum class LogKind { TX, RX, INFO, ERROR }

data class LogEntry(val time: Long, val kind: LogKind, val text: String)

/**
 * Collegamento BLE con un Robomow.
 *
 * Android esegue una sola operazione GATT per volta: ogni operazione passa da [gattLock]
 * e aspetta la propria callback prima di proseguire.
 */
@SuppressLint("MissingPermission")
class RobomowBle(context: Context, private val scope: CoroutineScope) {

    private val appContext = context.applicationContext
    private val adapter = appContext.getSystemService(BluetoothManager::class.java)?.adapter

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private val _config = MutableStateFlow<MowerConfig?>(null)
    val config: StateFlow<MowerConfig?> = _config.asStateFlow()

    private val _rsState = MutableStateFlow<RsState?>(null)
    val rsState: StateFlow<RsState?> = _rsState.asStateFlow()

    private val _lastUpdate = MutableStateFlow<Long?>(null)
    val lastUpdate: StateFlow<Long?> = _lastUpdate.asStateFlow()

    private val _log = MutableStateFlow<List<LogEntry>>(emptyList())
    val log: StateFlow<List<LogEntry>> = _log.asStateFlow()

    private val _scanResults = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val scanResults: StateFlow<List<ScannedDevice>> = _scanResults.asStateFlow()

    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning.asStateFlow()

    val isBluetoothOn: Boolean get() = adapter?.isEnabled == true

    private val gattLock = Mutex()
    @Volatile private var gatt: BluetoothGatt? = null
    private var dataOut: BluetoothGattCharacteristic? = null
    private var authChar: BluetoothGattCharacteristic? = null

    @Volatile private var pendingConnect: CompletableDeferred<Unit>? = null
    @Volatile private var pendingServices: CompletableDeferred<Unit>? = null
    @Volatile private var pendingWrite: CompletableDeferred<Int>? = null
    @Volatile private var pendingRead: CompletableDeferred<ByteArray?>? = null
    @Volatile private var pendingDescriptor: CompletableDeferred<Int>? = null

    private val parser = PacketParser { bytes, why -> log(LogKind.ERROR, "Scartato ($why): ${bytes.toHex()}") }
    private var counter = 0
    private var pollJob: Job? = null
    private var reconnectJob: Job? = null
    private var connectJob: Job? = null

    /** Ultimi parametri di connessione, per la riconnessione automatica. */
    private var target: Pair<String, String>? = null
    private var wantConnected = false
    private var reconnectAttempts = 0

    // --- Log ------------------------------------------------------------------------------

    fun log(kind: LogKind, text: String) {
        _log.update { (it + LogEntry(System.currentTimeMillis(), kind, text)).takeLast(MAX_LOG) }
    }

    fun clearLog() = _log.update { emptyList() }

    // --- Scansione ------------------------------------------------------------------------

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val uuids = result.scanRecord?.serviceUuids.orEmpty()
            val device = ScannedDevice(
                address = result.device.address,
                name = result.scanRecord?.deviceName ?: result.device.name,
                rssi = result.rssi,
                isRobomow = uuids.contains(ParcelUuid(UUID.fromString(RobomowProtocol.UUID_SERVICE))),
            )
            _scanResults.update { list ->
                (list.filterNot { it.address == device.address } + device)
                    .sortedWith(compareByDescending<ScannedDevice> { it.isRobomow }.thenByDescending { it.rssi })
            }
        }

        override fun onScanFailed(errorCode: Int) {
            log(LogKind.ERROR, "Scansione fallita (codice $errorCode)")
            _scanning.value = false
        }
    }

    fun startScan(durationMs: Long = 15_000) {
        val scanner = adapter?.bluetoothLeScanner ?: run {
            log(LogKind.ERROR, "Bluetooth non disponibile o spento")
            return
        }
        if (_scanning.value) return
        _scanResults.value = emptyList()
        _scanning.value = true
        log(LogKind.INFO, "Scansione Bluetooth avviata")
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner.startScan(null, settings, scanCallback)
        scope.launch {
            delay(durationMs)
            stopScan()
        }
    }

    fun stopScan() {
        if (!_scanning.value) return
        runCatching { adapter?.bluetoothLeScanner?.stopScan(scanCallback) }
        _scanning.value = false
        log(LogKind.INFO, "Scansione terminata: ${_scanResults.value.size} dispositivi")
    }

    // --- Connessione ----------------------------------------------------------------------

    fun connect(address: String, mainboardSerial: String) {
        target = address to mainboardSerial
        wantConnected = true
        reconnectAttempts = 0
        reconnectJob?.cancel()
        startConnect()
    }

    fun disconnect() {
        wantConnected = false
        reconnectJob?.cancel()
        connectJob?.cancel()
        teardown()
        _state.value = ConnectionState.Disconnected
        log(LogKind.INFO, "Disconnesso")
    }

    private fun startConnect() {
        val (address, serial) = target ?: return
        connectJob?.cancel()
        connectJob = scope.launch {
            try {
                doConnect(address, serial)
            } catch (e: Exception) {
                val reason = when (e) {
                    is TimeoutCancellationException -> "Il robot non risponde (tempo scaduto)"
                    else -> e.message ?: e.javaClass.simpleName
                }
                log(LogKind.ERROR, "Connessione fallita: $reason")
                teardown()
                _state.value = ConnectionState.Failed(reason)
                scheduleReconnect()
            }
        }
    }

    private suspend fun doConnect(address: String, serial: String) {
        val bt = adapter ?: error("Bluetooth non disponibile")
        if (!bt.isEnabled) error("Il Bluetooth e' spento")
        stopScan()
        teardown()
        parser.reset()
        _state.value = ConnectionState.Connecting
        log(LogKind.INFO, "Connessione a $address...")

        val device = bt.getRemoteDevice(address)
        val connected = CompletableDeferred<Unit>().also { pendingConnect = it }
        gatt = device.connectGatt(appContext, false, callback, BluetoothDevice.TRANSPORT_LE)
        withTimeout(20_000) { connected.await() }
        log(LogKind.INFO, "Collegato, ricerca servizi...")

        val g = gatt ?: error("Connessione persa")
        val discovered = CompletableDeferred<Unit>().also { pendingServices = it }
        if (!g.discoverServices()) error("Ricerca servizi non avviata")
        withTimeout(15_000) { discovered.await() }

        val service = g.getService(UUID.fromString(RobomowProtocol.UUID_SERVICE))
            ?: error("Servizio Robomow non trovato: e' il dispositivo giusto?")
        val auth = service.getCharacteristic(UUID.fromString(RobomowProtocol.UUID_CHAR_AUTH))
            ?: error("Caratteristica di autenticazione assente")
        val out = service.getCharacteristic(UUID.fromString(RobomowProtocol.UUID_CHAR_DATA_OUT))
            ?: error("Caratteristica dati (scrittura) assente")
        val dataIn = service.getCharacteristic(UUID.fromString(RobomowProtocol.UUID_CHAR_DATA_IN))
            ?: out.also { log(LogKind.INFO, "a506 assente, uso a503 per le notifiche") }
        authChar = auth
        dataOut = out

        _state.value = ConnectionState.Authenticating
        log(LogKind.INFO, "Autenticazione con il numero di serie della scheda madre...")
        val authStatus = writeCharacteristic(auth, RobomowProtocol.authPayload(serial), withResponse = true)
        if (authStatus != BluetoothGatt.GATT_SUCCESS) error("Scrittura autenticazione rifiutata (stato $authStatus)")
        val authResponse = readCharacteristic(auth)
        log(LogKind.RX, "AUTH ${authResponse?.toHex() ?: "(vuota)"}")
        if (!RobomowProtocol.isAuthOk(authResponse)) {
            wantConnected = false // con il seriale sbagliato non ha senso riprovare
            error("Autenticazione rifiutata: controlla il numero di serie della scheda madre")
        }

        enableNotifications(dataIn)
        _state.value = ConnectionState.Connected
        reconnectAttempts = 0
        log(LogKind.INFO, "Autenticato. Richiedo la configurazione del robot")
        send(RobomowProtocol.getConfig(), "GET_CONFIG")
        startPolling()
    }

    private fun scheduleReconnect() {
        if (!wantConnected || target == null) return
        reconnectJob?.cancel()
        val wait = (3_000L shl reconnectAttempts.coerceAtMost(4)).coerceAtMost(30_000L)
        reconnectAttempts++
        log(LogKind.INFO, "Nuovo tentativo tra ${wait / 1000} s")
        reconnectJob = scope.launch {
            delay(wait)
            if (wantConnected) startConnect()
        }
    }

    private fun teardown() {
        pollJob?.cancel()
        pollJob = null
        val failure = IllegalStateException("Connessione chiusa")
        listOf(pendingConnect, pendingServices, pendingWrite, pendingRead, pendingDescriptor)
            .forEach { it?.completeExceptionally(failure) }
        pendingConnect = null; pendingServices = null; pendingWrite = null; pendingRead = null; pendingDescriptor = null
        gatt?.let {
            runCatching { it.disconnect() }
            runCatching { it.close() }
        }
        gatt = null
        dataOut = null
        authChar = null
    }

    // --- Invio ----------------------------------------------------------------------------

    private fun nextCounter(): Int {
        counter = (counter + 1) and 0xFFFF
        if (counter == 0) counter = 1
        return counter
    }

    /** Invia un pacchetto gia' completo (anche dalla pagina Diagnostica). */
    fun send(packet: ByteArray, label: String? = null) {
        scope.launch {
            val ch = dataOut
            if (ch == null || _state.value !is ConnectionState.Connected) {
                log(LogKind.ERROR, "Non collegato: pacchetto non inviato")
                return@launch
            }
            val name = label ?: if (packet.size >= 4) RobomowProtocol.Type.name(packet[3].toInt() and 0xFF) else "RAW"
            log(LogKind.TX, "$name ${packet.toHex()}")
            try {
                val withResponse = ch.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0
                val status = writeCharacteristic(ch, packet, withResponse)
                if (status != BluetoothGatt.GATT_SUCCESS) log(LogKind.ERROR, "Scrittura rifiutata (stato $status)")
            } catch (e: Exception) {
                log(LogKind.ERROR, "Errore di scrittura: ${e.message}")
            }
        }
    }

    fun sendOperation(mode: OperationMode) {
        send(RobomowProtocol.autoOperation(nextCounter(), mode), "AUTO_OP ${mode.name}")
    }

    fun requestRsState() {
        send(RobomowProtocol.rsStateRequest(nextCounter()), "STATO_RS")
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            // Aspetta la risposta a GET_CONFIG prima di decidere cosa chiedere.
            delay(1_500)
            while (isActive && _state.value is ConnectionState.Connected) {
                val family = _config.value?.family
                if (family == null || family == MowerFamily.RS) {
                    // Su un RS (o famiglia ancora sconosciuta) proviamo la richiesta di stato RS.
                    requestRsState()
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    // --- Ricezione ------------------------------------------------------------------------

    private fun onData(data: ByteArray) {
        for (packet in parser.feed(data)) handlePacket(packet)
    }

    private fun handlePacket(packet: Packet) {
        when (packet.type) {
            RobomowProtocol.Type.GET_CONFIG -> {
                val cfg = MowerConfig.parse(packet.payload)
                log(LogKind.RX, "GET_CONFIG ${packet.raw.toHex()}")
                if (cfg != null) {
                    _config.value = cfg
                    log(
                        LogKind.INFO,
                        "Famiglia ${cfg.family} (codice ${cfg.familyCode}), software ${cfg.softwareLabel}, " +
                            "scheda madre v${cfg.mainboardVersion}",
                    )
                }
            }

            RobomowProtocol.Type.MISCELLANEOUS -> {
                val s = RsState.parse(packet)
                if (s != null) {
                    // Lo stato arriva ogni 3 s: nel log solo quando cambia.
                    if (s != _rsState.value) log(LogKind.RX, "STATO_RS ${packet.raw.toHex()}")
                    _rsState.value = s
                    _lastUpdate.value = System.currentTimeMillis()
                } else {
                    log(LogKind.RX, "MISC ${packet.raw.toHex()}")
                }
            }

            else -> log(LogKind.RX, "${packet.typeName} ${packet.raw.toHex()}")
        }
    }

    // --- Operazioni GATT serializzate ------------------------------------------------------

    private suspend fun writeCharacteristic(
        ch: BluetoothGattCharacteristic,
        value: ByteArray,
        withResponse: Boolean,
    ): Int = gattLock.withLock {
        val g = gatt ?: error("Non collegato")
        val done = CompletableDeferred<Int>().also { pendingWrite = it }
        val type = if (withResponse) BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        else BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        val started = if (Build.VERSION.SDK_INT >= 33) {
            g.writeCharacteristic(ch, value, type) == BluetoothGatt.GATT_SUCCESS
        } else {
            ch.writeType = type
            ch.value = value
            g.writeCharacteristic(ch)
        }
        if (!started) {
            pendingWrite = null
            error("Scrittura non avviata")
        }
        try {
            withTimeout(if (withResponse) 5_000L else 1_000L) { done.await() }
        } catch (e: TimeoutCancellationException) {
            if (withResponse) throw e else BluetoothGatt.GATT_SUCCESS
        } finally {
            pendingWrite = null
        }
    }

    private suspend fun readCharacteristic(ch: BluetoothGattCharacteristic): ByteArray? = gattLock.withLock {
        val g = gatt ?: error("Non collegato")
        val done = CompletableDeferred<ByteArray?>().also { pendingRead = it }
        if (!g.readCharacteristic(ch)) {
            pendingRead = null
            error("Lettura non avviata")
        }
        try {
            withTimeout(5_000) { done.await() }
        } finally {
            pendingRead = null
        }
    }

    private suspend fun enableNotifications(ch: BluetoothGattCharacteristic) = gattLock.withLock {
        val g = gatt ?: error("Non collegato")
        if (!g.setCharacteristicNotification(ch, true)) error("Notifiche non attivabili")
        val cccd = ch.getDescriptor(UUID.fromString(RobomowProtocol.UUID_CCCD)) ?: return@withLock
        val value = if (ch.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        else BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        val done = CompletableDeferred<Int>().also { pendingDescriptor = it }
        val started = if (Build.VERSION.SDK_INT >= 33) {
            g.writeDescriptor(cccd, value) == BluetoothGatt.GATT_SUCCESS
        } else {
            cccd.value = value
            g.writeDescriptor(cccd)
        }
        if (!started) {
            pendingDescriptor = null
            error("Attivazione notifiche non avviata")
        }
        try {
            val status = withTimeout(5_000) { done.await() }
            if (status != BluetoothGatt.GATT_SUCCESS) error("Notifiche rifiutate (stato $status)")
        } finally {
            pendingDescriptor = null
        }
    }

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (g != gatt) return
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> pendingConnect?.complete(Unit)
                BluetoothProfile.STATE_DISCONNECTED -> scope.launch {
                    val wasConnected = _state.value is ConnectionState.Connected
                    log(LogKind.INFO, "Collegamento Bluetooth chiuso (stato $status)")
                    teardown()
                    if (wasConnected) {
                        _state.value = ConnectionState.Failed("Collegamento perso (fuori portata?)")
                        scheduleReconnect()
                    }
                }
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) pendingServices?.complete(Unit)
            else pendingServices?.completeExceptionally(IllegalStateException("Ricerca servizi fallita ($status)"))
        }

        override fun onCharacteristicWrite(g: BluetoothGatt, ch: BluetoothGattCharacteristic, status: Int) {
            pendingWrite?.complete(status)
        }

        override fun onCharacteristicRead(
            g: BluetoothGatt,
            ch: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int,
        ) {
            pendingRead?.complete(if (status == BluetoothGatt.GATT_SUCCESS) value else null)
        }

        @Deprecated("Android 12 e precedenti")
        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(g: BluetoothGatt, ch: BluetoothGattCharacteristic, status: Int) {
            if (Build.VERSION.SDK_INT >= 33) return
            pendingRead?.complete(if (status == BluetoothGatt.GATT_SUCCESS) ch.value else null)
        }

        override fun onDescriptorWrite(g: BluetoothGatt, d: BluetoothGattDescriptor, status: Int) {
            pendingDescriptor?.complete(status)
        }

        override fun onCharacteristicChanged(g: BluetoothGatt, ch: BluetoothGattCharacteristic, value: ByteArray) {
            val copy = value.copyOf()
            scope.launch { onData(copy) }
        }

        @Deprecated("Android 12 e precedenti")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(g: BluetoothGatt, ch: BluetoothGattCharacteristic) {
            if (Build.VERSION.SDK_INT >= 33) return
            val copy = ch.value?.copyOf() ?: return
            scope.launch { onData(copy) }
        }
    }

    fun close() {
        wantConnected = false
        stopScan()
        teardown()
    }

    companion object {
        private const val MAX_LOG = 3_000
        const val POLL_INTERVAL_MS = 3_000L
    }
}
