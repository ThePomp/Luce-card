package it.luce.robomow.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.luce.robomow.MowerSettings
import it.luce.robomow.MowerViewModel
import it.luce.robomow.ble.ConnectionState
import it.luce.robomow.ble.LogEntry
import it.luce.robomow.ble.LogKind
import it.luce.robomow.ble.ScannedDevice
import it.luce.robomow.protocol.MowerConfig
import it.luce.robomow.protocol.RobomowProtocol
import it.luce.robomow.protocol.RobomowProtocol.OperationMode
import it.luce.robomow.protocol.RsState
import it.luce.robomow.protocol.parseHex
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Green = Color(0xFF7BC86C)
private val Amber = Color(0xFFF2B33D)
private val Red = Color(0xFFE5534B)
private val PanelColor = Color(0xFF1A201C)
private val Muted = Color(0xFF8C9A90)

private val colors = darkColorScheme(
    primary = Green,
    onPrimary = Color(0xFF0B1A0D),
    secondary = Amber,
    error = Red,
    background = Color(0xFF101412),
    surface = Color(0xFF101412),
    surfaceVariant = PanelColor,
    onSurface = Color(0xFFE8F0EA),
    onSurfaceVariant = Muted,
)

private enum class Tab(val label: String) { ROBOT("Robot"), DIAG("Diagnostica"), SETTINGS("Impostazioni") }

@Composable
fun RobomowApp(
    viewModel: MowerViewModel,
    permissionsGranted: Boolean,
    onRequestPermissions: () -> Unit,
    onShareText: (String, String) -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(if (settings.isComplete) Tab.ROBOT else Tab.SETTINGS) }

    MaterialTheme(colorScheme = colors) {
        Scaffold(
            containerColor = colors.background,
            bottomBar = {
                NavigationBar(containerColor = PanelColor) {
                    Tab.entries.forEach { t ->
                        NavigationBarItem(
                            selected = tab == t,
                            onClick = { tab = t },
                            label = { Text(t.label) },
                            icon = {
                                Icon(
                                    when (t) {
                                        Tab.ROBOT -> Icons.Filled.Home
                                        Tab.DIAG -> Icons.Filled.Build
                                        Tab.SETTINGS -> Icons.Filled.Settings
                                    },
                                    contentDescription = t.label,
                                )
                            },
                        )
                    }
                }
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                if (!permissionsGranted) {
                    PermissionScreen(onRequestPermissions)
                } else {
                    when (tab) {
                        Tab.ROBOT -> RobotScreen(viewModel, settings) { tab = Tab.SETTINGS }
                        Tab.DIAG -> DiagnosticsScreen(viewModel, onShareText)
                        Tab.SETTINGS -> SettingsScreen(viewModel, settings)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionScreen(onRequest: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Serve il permesso Bluetooth", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "L'app parla col tosaerba via Bluetooth e non usa internet ne' la posizione.",
            color = Muted,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequest) { Text("Concedi permesso") }
    }
}

// --- Robot ---------------------------------------------------------------------------------

@Composable
private fun RobotScreen(viewModel: MowerViewModel, settings: MowerSettings, openSettings: () -> Unit) {
    val state by viewModel.ble.state.collectAsStateWithLifecycle()
    val config by viewModel.ble.config.collectAsStateWithLifecycle()
    val rs by viewModel.ble.rsState.collectAsStateWithLifecycle()
    val lastUpdate by viewModel.ble.lastUpdate.collectAsStateWithLifecycle()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ConnectionCard(state, settings, viewModel::connect, viewModel::disconnect, openSettings)

        val connected = state is ConnectionState.Connected
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BatteryDial(rs?.batteryPercent, rs?.isCharging == true)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        rs?.systemModeLabel ?: if (connected) "In attesa di dati..." else "--",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    StatusLine("Lama", rs?.let { if (it.bladeMotorActive) "In rotazione" else "Ferma" })
                    StatusLine("Ricarica", rs?.let { if (it.isCharging) "Sulla base" else "No" })
                    StatusLine("Antifurto", rs?.let { if (it.antiTheftActive) "Attivo" else "Spento" })
                }
            }
        }

        Panel {
            Text("Programma", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            StatusLine("Prossima partenza", rs?.minutesToNextDeparture?.let(::formatMinutes))
            StatusLine("Durata ciclo", rs?.autoOperationMinutes?.let(::formatMinutes))
            StatusLine("Ultimo aggiornamento", lastUpdate?.let { timeFormat.format(Date(it)) })
        }

        CommandsCard(enabled = connected, onCommand = viewModel::command)

        config?.let { InfoCard(it) }

        Text(
            "Stato e comandi della serie RS sono ricavati da progetti della comunita' e vanno " +
                "confermati sul tuo robot: in caso di dubbio controlla la pagina Diagnostica.",
            color = Muted,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ConnectionCard(
    state: ConnectionState,
    settings: MowerSettings,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    openSettings: () -> Unit,
) {
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val (color, label) = when (state) {
                ConnectionState.Connected -> Green to "Collegato"
                ConnectionState.Connecting -> Amber to "Connessione..."
                ConnectionState.Authenticating -> Amber to "Autenticazione..."
                ConnectionState.Disconnected -> Muted to "Non collegato"
                is ConnectionState.Failed -> Red to "Errore"
            }
            Box(Modifier.size(12.dp).background(color, CircleShape))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.SemiBold)
                Text(
                    settings.deviceName.ifBlank { settings.deviceAddress.ifBlank { "Nessun robot scelto" } },
                    color = Muted,
                    fontSize = 13.sp,
                )
            }
            when {
                !settings.isComplete -> Button(onClick = openSettings) { Text("Configura") }
                state is ConnectionState.Connected ||
                    state is ConnectionState.Connecting ||
                    state is ConnectionState.Authenticating ->
                    OutlinedButton(onClick = onDisconnect) { Text("Disconnetti") }
                else -> Button(onClick = onConnect) { Text("Connetti") }
            }
        }
        if (state is ConnectionState.Failed) {
            Spacer(Modifier.height(8.dp))
            Text(state.reason, color = Red, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CommandsCard(enabled: Boolean, onCommand: (OperationMode) -> Unit) {
    Panel {
        Text("Comandi", fontWeight = FontWeight.SemiBold)
        Text("Tocca due volte per confermare (tranne Stop)", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ArmedButton("Taglia", enabled, Modifier.weight(1f)) { onCommand(OperationMode.MOW) }
            ArmedButton("Bordo", enabled, Modifier.weight(1f)) { onCommand(OperationMode.EDGE) }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ArmedButton("Alla base", enabled, Modifier.weight(1f)) { onCommand(OperationMode.HOME) }
            Button(
                onClick = { onCommand(OperationMode.STOP) },
                enabled = enabled,
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red, contentColor = Color.White),
            ) { Text("STOP", fontWeight = FontWeight.Bold) }
        }
    }
}

/** Primo tocco: arma il tasto per 3 secondi. Secondo tocco: invia. */
@Composable
private fun ArmedButton(label: String, enabled: Boolean, modifier: Modifier, onFire: () -> Unit) {
    var armedAt by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(armedAt) {
        if (armedAt != null) {
            delay(3_000)
            armedAt = null
        }
    }
    val armed = armedAt != null
    Button(
        onClick = {
            if (armed) {
                armedAt = null
                onFire()
            } else {
                armedAt = System.currentTimeMillis()
            }
        },
        enabled = enabled,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (armed) Amber else Green,
        ),
    ) { Text(if (armed) "Conferma" else label, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun InfoCard(cfg: MowerConfig) {
    Panel {
        Text("Robot", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        StatusLine("Famiglia", "${cfg.family} (codice ${cfg.familyCode})")
        StatusLine("Software", cfg.softwareLabel)
        StatusLine("Scheda madre", "v${cfg.mainboardVersion}")
    }
}

@Composable
private fun BatteryDial(percent: Int?, charging: Boolean) {
    val color = when {
        percent == null -> Muted
        percent < 20 -> Red
        percent < 45 -> Amber
        else -> Green
    }
    Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = Color(0xFF2A322D), startAngle = 135f, sweepAngle = 270f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round),
            )
            if (percent != null) {
                drawArc(
                    color = color, startAngle = 135f, sweepAngle = 270f * percent.coerceIn(0, 100) / 100f,
                    useCenter = false, topLeft = Offset(inset, inset), size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(percent?.let { "$it%" } ?: "--", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(if (charging) "in carica" else "batteria", color = Muted, fontSize = 12.sp)
        }
    }
}

// --- Diagnostica -----------------------------------------------------------------------------

@Composable
private fun DiagnosticsScreen(viewModel: MowerViewModel, onShareText: (String, String) -> Unit) {
    val log by viewModel.ble.log.collectAsStateWithLifecycle()
    val state by viewModel.ble.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    var hex by rememberSaveable { mutableStateOf("") }
    var addChecksum by rememberSaveable { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val connected = state is ConnectionState.Connected

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Invia pacchetto", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = hex,
            onValueChange = { hex = it; error = null },
            label = { Text("Esadecimale, es. AA 05 1F 0F") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            isError = error != null,
            supportingText = if (error != null) { { Text(error.orEmpty()) } } else null,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = addChecksum, onCheckedChange = { addChecksum = it })
            Text("Aggiungi lunghezza e checksum", fontSize = 13.sp, modifier = Modifier.weight(1f))
            Button(enabled = connected, onClick = {
                try {
                    var bytes = parseHex(hex)
                    if (addChecksum) {
                        // L'utente scrive tipo + dati (es. "0F"), noi completiamo il pacchetto.
                        val body = if (bytes.size >= 3 && (bytes[0].toInt() and 0xFF) == 0xAA) bytes.copyOfRange(3, bytes.size) else bytes
                        bytes = RobomowProtocol.frame(body[0].toInt() and 0xFF, body.copyOfRange(1, body.size))
                    }
                    viewModel.ble.send(bytes)
                } catch (e: Exception) {
                    error = e.message
                }
            }) { Text("Invia") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(enabled = connected, onClick = { viewModel.ble.send(RobomowProtocol.getConfig(), "GET_CONFIG") }) {
                Text("Config")
            }
            OutlinedButton(enabled = connected, onClick = { viewModel.ble.requestRsState() }) { Text("Stato RS") }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = PanelColor)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Registro (${log.size})", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = { clipboard.setText(AnnotatedString(formatLog(log))) }) { Text("Copia") }
            TextButton(onClick = { onShareText("Registro Robomow", formatLog(log)) }) { Text("Condividi") }
            TextButton(onClick = viewModel.ble::clearLog) { Text("Pulisci") }
        }

        val listState = rememberLazyListState()
        LaunchedEffect(log.size) { if (log.isNotEmpty()) listState.scrollToItem(log.size - 1) }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().background(PanelColor, RoundedCornerShape(8.dp)),
            contentPadding = PaddingValues(8.dp),
        ) {
            items(log) { LogRow(it) }
        }
    }
}

@Composable
private fun LogRow(entry: LogEntry) {
    val color = when (entry.kind) {
        LogKind.TX -> Amber
        LogKind.RX -> Green
        LogKind.INFO -> Muted
        LogKind.ERROR -> Red
    }
    Text(
        "${timeFormat.format(Date(entry.time))} ${entry.kind.name.padEnd(5)} ${entry.text}",
        color = color,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    )
}

// --- Impostazioni -------------------------------------------------------------------------------

@Composable
private fun SettingsScreen(viewModel: MowerViewModel, settings: MowerSettings) {
    val scanning by viewModel.ble.scanning.collectAsStateWithLifecycle()
    val devices by viewModel.ble.scanResults.collectAsStateWithLifecycle()
    var serial by rememberSaveable { mutableStateOf(settings.mainboardSerial) }
    var showSerial by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Panel {
            Text("1. Numero di serie della scheda madre", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Non e' il numero sull'etichetta. Accedi a myrobomow.robomow.com dal computer, " +
                    "premi F12, scheda Rete (Network), ricarica la pagina, apri la richiesta " +
                    "\"products\" e copia il campo MainboardSerial.",
                color = Muted,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = serial,
                onValueChange = { serial = it.filter { c -> c.isLetterOrDigit() } },
                label = { Text("MainboardSerial") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                visualTransformation = if (showSerial) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showSerial = !showSerial }) { Text(if (showSerial) "Nascondi" else "Mostra") }
                },
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (settings.mainboardSerial.isNotBlank()) "Salvato" else "Non salvato",
                    color = if (settings.mainboardSerial.isNotBlank()) Green else Muted,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                )
                Button(enabled = serial.isNotBlank() && serial != settings.mainboardSerial, onClick = {
                    viewModel.saveSerial(serial)
                }) { Text("Salva") }
            }
        }

        Panel {
            Text("2. Scegli il robot", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Stai vicino al tosaerba acceso e chiudi l'app Robomow ufficiale: il robot accetta " +
                    "un solo collegamento per volta.",
                color = Muted,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    settings.deviceName.ifBlank { settings.deviceAddress.ifBlank { "Nessuno" } },
                    modifier = Modifier.weight(1f),
                )
                if (scanning) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = viewModel.ble::stopScan) { Text("Ferma") }
                } else {
                    Button(onClick = { viewModel.ble.startScan() }) { Text("Cerca") }
                }
            }
            devices.forEach { d ->
                DeviceRow(d, selected = d.address == settings.deviceAddress) { viewModel.selectDevice(d) }
            }
        }

        Text(
            "Progetto non ufficiale, senza legami con Robomow/MTD/Stiga. I comandi agiscono su " +
                "una macchina con lame: usali solo con il robot in vista.",
            color = Muted,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun DeviceRow(device: ScannedDevice, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                device.name ?: "(senza nome)",
                fontWeight = if (device.isRobomow) FontWeight.SemiBold else FontWeight.Normal,
                color = if (device.isRobomow) Green else MaterialTheme.colorScheme.onSurface,
            )
            Text("${device.address}  ${device.rssi} dBm", color = Muted, fontSize = 12.sp)
        }
        if (device.isRobomow) Text("Robomow", color = Green, fontSize = 12.sp)
        if (selected) Text("  scelto", color = Amber, fontSize = 12.sp)
    }
}

// --- Componenti comuni -------------------------------------------------------------------------

@Composable
private fun Panel(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PanelColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun StatusLine(label: String, value: String?) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = Muted, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value ?: "--", fontSize = 14.sp)
    }
}

private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ITALY)

private fun formatMinutes(min: Int): String = when {
    min < 60 -> "$min min"
    min < 60 * 24 -> "${min / 60} h ${min % 60} min"
    else -> "${min / (60 * 24)} g ${(min / 60) % 24} h"
}

private fun formatLog(log: List<LogEntry>): String {
    val f = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ITALY)
    return log.joinToString("\n") { "${f.format(Date(it.time))} ${it.kind.name} ${it.text}" }
}
