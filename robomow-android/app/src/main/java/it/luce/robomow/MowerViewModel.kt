package it.luce.robomow

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.luce.robomow.ble.RobomowBle
import it.luce.robomow.ble.ScannedDevice
import it.luce.robomow.protocol.RobomowProtocol.OperationMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MowerSettings(
    val mainboardSerial: String = "",
    val deviceAddress: String = "",
    val deviceName: String = "",
) {
    val isComplete: Boolean get() = mainboardSerial.isNotBlank() && deviceAddress.isNotBlank()
}

class MowerViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("robomow", Context.MODE_PRIVATE)

    val ble = RobomowBle(app, viewModelScope)

    private val _settings = MutableStateFlow(
        MowerSettings(
            mainboardSerial = prefs.getString(KEY_SERIAL, "").orEmpty(),
            deviceAddress = prefs.getString(KEY_ADDRESS, "").orEmpty(),
            deviceName = prefs.getString(KEY_NAME, "").orEmpty(),
        ),
    )
    val settings: StateFlow<MowerSettings> = _settings.asStateFlow()

    fun saveSerial(serial: String) {
        val clean = serial.trim()
        prefs.edit().putString(KEY_SERIAL, clean).apply()
        _settings.value = _settings.value.copy(mainboardSerial = clean)
    }

    fun selectDevice(device: ScannedDevice) {
        prefs.edit()
            .putString(KEY_ADDRESS, device.address)
            .putString(KEY_NAME, device.name.orEmpty())
            .apply()
        _settings.value = _settings.value.copy(deviceAddress = device.address, deviceName = device.name.orEmpty())
        ble.stopScan()
    }

    fun connect() {
        val s = _settings.value
        if (s.isComplete) ble.connect(s.deviceAddress, s.mainboardSerial)
    }

    fun disconnect() = ble.disconnect()

    fun command(mode: OperationMode) = ble.sendOperation(mode)

    override fun onCleared() {
        ble.close()
    }

    private companion object {
        const val KEY_SERIAL = "mainboard_serial"
        const val KEY_ADDRESS = "device_address"
        const val KEY_NAME = "device_name"
    }
}
