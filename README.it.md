# Luce Garage Card

![Anteprima di Luce Garage Card](preview.png)

Card Lovelace compatta in stile strumentazione per auto elettriche: quadranti analogici per batteria, autonomia e 12 V, tasti fisici, stato di porte e finestrini e programmazione settimanale. Le auto restano affiancate anche col telefono in verticale.

Pensata per l'integrazione [Kia Uvo / Hyundai Bluelink](https://github.com/Hyundai-Kia-Connect/kia_uvo). Un solo file JavaScript, nessuna dipendenza, italiano e inglese. Tutte le opzioni sono nel [README inglese](README.md).

> Progetto non ufficiale, senza legami con Ferrari, Hyundai, Kia o altri costruttori. Le integrazioni delle auto usano API non ufficiali che possono smettere di funzionare. I comandi remoti agiscono su un'auto vera: usali con attenzione.

## Installazione

**HACS:** aggiungi questo repository come repository personalizzato, categoria **Dashboard**, scarica **Luce Garage Card**, poi chiudi del tutto l'app Home Assistant e riaprila.

Se subito dopo compare "Errore di configurazione", l'interfaccia non ha ancora caricato il file: chiudi completamente l'app e riaprila.

## Configurazione

Se le entità hanno i nomi italiani di `kia_uvo`, ad esempio `sensor.mia_auto_livello_batteria_ev`:

```yaml
type: custom:luce-garage-card
cars:
  - name: La mia auto
    prefix: mia_auto
    schedule_id: mia_auto
    image: /local/mia_auto.webp   # facoltativa, meglio con sfondo trasparente
    max_range: 500
    entities:
      heat: script.mia_auto_heat
```

Un tasto o una sezione spariscono se la relativa entità non esiste. Per occupare tutta la pagina usa una vista di tipo *panel*.

## Riscaldamento e programmazione

[`luce_garage.yaml`](luce_garage.yaml) crea helper, script e automazione per un'auto. Sostituisci `my_ev` con il tuo identificativo (anche nello `schedule_id`) e `REPLACE_WITH_DEVICE_ID`, che trovi nell'indirizzo della pagina del dispositivo. Le opzioni di riscaldamento cambiano da modello a modello: provale con l'auto vicina. La programmazione parte spenta.

Scalda, Sblocca, Aggiorna, Apri e Socchiudi richiedono due tocchi: il primo arma il tasto per 3 secondi, il secondo invia il comando.

## Licenza

MIT

---

# Luce Wallet Card

Card nello stesso stile per il patrimonio: valore totale grande, variazione per periodo (1G, 1S, 1M, 6M, 1A) con grafico interattivo, e ripartizione per conto. Tocca il totale per nascondere gli importi (la scelta resta salvata sul dispositivo); tocca un conto per aprirne i dettagli.

## Installazione

Copia `luce-wallet-card.js` in `/config/www/` e aggiungi la risorsa `/local/luce-wallet-card.js` di tipo *Modulo JavaScript* (Impostazioni → Dashboard → ⋮ → Risorse), poi chiudi e riapri l'app.

## Configurazione

```yaml
type: custom:luce-wallet-card
title: Patrimonio
total: sensor.patrimonio_netto_totale     # serve lo storico: state_class measurement
change: sensor.variazione_oggi            # facoltativo: % (o importo) per "1G"
accounts:
  - name: Broker
    entity: sensor.broker_valore_totale
    change: sensor.broker_rendimento_giorno   # facoltativo, % o importo
    change_label: oggi
  - name: Conto corrente
    entity: [sensor.conto_1, sensor.conto_2]  # più entità vengono sommate
  - name: Fondo pensione
    entity: input_number.fondo_pensione
    in_total: false                           # mostrato a parte, fuori dalla ripartizione
```

Le variazioni 1S, 1M, 6M e 1A sono calcolate dalle statistiche a lungo termine del totale: includono quindi anche versamenti e prelievi, non solo il rendimento. Altre opzioni: `history` (entità diversa per il grafico), `default_period` (`day`, `week`, `month`, `half`, `year`), `show_periods: false`, `currency`, `language`, `color` e `subtitle` per ogni conto. I colori seguono l'ordine dei conti nella configurazione (fino a 8).
