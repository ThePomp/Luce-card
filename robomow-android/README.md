# Robomow Luce (app Android, anteprima)

App Android non ufficiale per i Robomow con solo Bluetooth (pensata per la serie RS, ad esempio RS625).
Si collega direttamente al robot: niente cloud, niente account, niente ESP32.

> Progetto non ufficiale, senza legami con Robomow, MTD o Stiga. Il protocollo è ricostruito dalla comunità:
> i comandi agiscono su una macchina con lame, usali solo con il robot in vista.

## Installazione

Scarica `robomow-luce.apk` dalla release **robomow-app-latest** del repository e aprilo dal telefono
(Android 8 o successivo; consenti "installa app sconosciute").

## Primo avvio

1. **Numero di serie della scheda madre** (non è quello sull'etichetta): accedi a myrobomow.robomow.com
   dal computer, premi F12, scheda *Rete/Network*, ricarica, apri la richiesta `products` e copia
   `MainboardSerial`.
2. **Impostazioni → Cerca**, vicino al robot acceso e con l'app ufficiale chiusa. Il Robomow compare in verde.
3. **Robot → Connetti.**

## Cosa fa

| Funzione | Stato |
|---|---|
| Collegamento e autenticazione | da progetti testati su RS615u e RT |
| Famiglia, versione software, scheda madre | messaggio comune a tutti i modelli |
| Batteria, ricarica, lama, modalità, prossima partenza | formato RS da RoboMow2MQTT: **da verificare** |
| Taglia / Bordo / Alla base / Stop | formato preso dalla serie RC: **da verificare** |
| Programmazione settimanale | dopo l'analisi del log Bluetooth dell'app ufficiale |
| Diagnostica: registro esadecimale, invio pacchetti, condivisione | sì |

Riconnessione automatica se il robot esce dalla portata. Taglia, Bordo e Alla base chiedono due tocchi
(il primo arma il tasto per 3 secondi); Stop funziona con un tocco solo.

## Struttura

- `protocol/` — protocollo in Kotlin puro, con test (`./gradlew :protocol:test`, senza SDK Android).
- `app/` — app Jetpack Compose (`./gradlew :app:assembleDebug`, richiede l'SDK Android).

L'APK è compilato da GitHub Actions (`.github/workflows/robomow-android.yml`).

## Fonti

- [arjanmels/robomow_ble](https://github.com/arjanmels/robomow_ble): UUID, formato dei pacchetti, GET_CONFIG
- [lutskov/RobomowRemoteControl](https://github.com/lutskov/RobomowRemoteControl): autenticazione su RS615u
- [TheCutter/RoboMow2MQTT](https://github.com/TheCutter/RoboMow2MQTT): stato e comandi della serie RS
