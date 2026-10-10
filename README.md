# 🚦 NSTP: SpDr Nummern-Stellpult

> **Hinweis:** *Stellwerk nicht inkludiert*
> Der Mod drückt die Tasten, aber ob die Fahrstraße auch wirklich gesichert ist, entscheidet deine Relaisschaltung. Wer vergessen hat, die Gegenfahrstraße zu verschließen, bekommt trotzdem ein freundliches „Klick Klack!".

Ein serverseitiger Forge-Mod für **Minecraft 1.12.2**, mit dem Spieler per Chat-Nachricht Tastenrelais in einem Stellwerk schalten. Statt zum Pult zu laufen, tippt man eine Nummer in den Chat. Der Mod schaltet dafür die passenden **Wireless-Redstone-Frequenzen** für eine Sekunde ein.

---

## ✨ Funktionen

- Chat-Eingabe im Format `XXXYYY<Symbol>`, zum Beispiel `001002+`
- Pro Stellwerk eigene Zuordnung von Zahl zu Frequenz, getrennt nach den Symbolen `-`, `--`, `+` und `++`
- Passende Nachrichten erscheinen **nicht** im öffentlichen Chat
- Die Eingabe wird **komplett verworfen**, wenn eine der beiden Zahlen nicht in der Config steht, und es wird nichts geschaltet
- `/nstp reset` setzt die Frequenzen eines Stellwerks zurück, automatisch auch nach jedem Serverstart (mit Meldung im Serverlog)
- Reiner **Server-Mod**: Spieler brauchen ihn nicht installiert

---

## 📦 Voraussetzungen

| Was | Version |
| --- | --- |
| Minecraft | 1.12.2 |
| Minecraft Forge | 1.12.2-14.23.5.2864 |
| [Wireless Redstone](https://github.com/Razzokk/wireless-redstone) (Razzokk) | 1.12.2-Version, nur auf dem Server |

---

## 🔧 Installation

1. Das NSTP-Jar und das Wireless-Redstone-Jar in den Ordner `mods/` des **Servers** legen.
2. Server einmal starten. Dabei wird `config/nstp.json` mit einem Beispiel angelegt.
3. Server stoppen, die Config an dein Stellwerk anpassen, Server wieder starten.

Clients brauchen den Mod **nicht**. Sie müssen aber Wireless Redstone installiert haben, wenn sie dessen Blöcke und Items sehen sollen.

---

## 🎮 Benutzung

### Befehle

| Befehl | Wer | Wirkung |
| --- | --- | --- |
| `/nstp <stellwerk> on` | alle | Chat-Überwachung für dieses Stellwerk einschalten |
| `/nstp <stellwerk> off` | alle | Chat-Überwachung ausschalten |
| `/nstp reset <stellwerk>` | Op | Alle Frequenzen des Stellwerks zurücksetzen |
| `/nstp reset all` | Op | Alle Stellwerke zurücksetzen |

Stellwerksnamen dürfen keine Leerzeichen enthalten, und die Groß-/Kleinschreibung ist egal. Ein Stellwerk darf nicht `reset` heißen.

### Eingabeschema

```
XXXYYY<Symbol>
```

Das sind sechs Ziffern, gefolgt von `-`, `--`, `+` oder `++`. Beide Zahlen werden gleichzeitig geschaltet.

| Symbol | Meldung im Spiel |
| --- | --- |
| `+`, `-` | Zugfahrstraßen-Tastenrelais |
| `++`, `--` | Rangierfahrstraßen-Tastenrelais |

### Beispiel

```
/nstp Villingen on
001002+
```

Antwort:

```
[NSTP] Villingen: Zugfahrstraßen-Tastenrelais 001 und 002 geschaltet. Klick Klack!
```

Fehlt eine der Zahlen in der Config:

```
[NSTP] Eingabe verworfen: 002 fehlt in der Config (Villingen +). Es wurde nichts geschaltet.
```

---

## ⚙️ Konfiguration

Die Datei liegt unter `config/nstp.json`:

```json
{
  "pulseTicks": 20,
  "stellwerke": {
    "Villingen": {
      "dimension": 0,
      "-":  { "001": 1001, "002": 1002 },
      "--": { "001": 2001, "002": 2002 },
      "+":  { "001": 3001, "002": 3002 },
      "++": { "001": 4001, "002": 4002 }
    }
  }
}
```

| Feld | Bedeutung |
| --- | --- |
| `pulseTicks` | Dauer des Signals in Ticks (20 Ticks = 1 Sekunde) |
| `dimension` | Dimension, in der deine Receiver stehen (0 = Overworld) |
| `"-"`, `"--"`, `"+"`, `"++"` | Zuordnung Zahl → Frequenz |

**Wichtig:**
- Die Zahlen sind **Strings** (`"001"`), damit führende Nullen erhalten bleiben.
- Frequenzen müssen zwischen **0 und 65535** liegen.
- `dimension` muss die Dimension sein, in der die Receiver stehen. Wireless Redstone prüft Receiver nur in der Welt des letzten Zugriffs.

---

## 🧯 Wenn mal eine Frequenz hängt

Der Mod meldet sich bei Wireless Redstone wie ein gedrückter **Remote** an und nach `pulseTicks` wieder ab. Stürzt der Server mitten in einem Impuls ab, kann so eine Anmeldung stehen bleiben.

- Beim nächsten Serverstart setzt NSTP automatisch alle Frequenzen aus der Config zurück. Pro Stellwerk steht eine Zeile im Log.
- Von Hand geht es mit `/nstp reset <stellwerk>`.
- Für alles andere gibt es `/wr clear <frequenz>` oder `/wr clear all` aus Wireless Redstone.

**Achtung:** Der Reset meldet auch echte Transmitter auf diesen Frequenzen ab. Auf Frequenzen, die in der Config stehen, sollten deshalb keine dauerhaft aktiven Transmitter liegen.

---

## 🛠️ Selbst bauen

1. Forge-MDK 1.12.2 (14.23.5.2864) entpacken.
2. Das Wireless-Redstone-Jar nach `libs/` legen.
3. In der `build.gradle` ergänzen:

   ```groovy
   dependencies {
       compileOnly fileTree(dir: 'libs', include: ['*.jar'])
   }

   tasks.withType(JavaCompile) {
       options.encoding = 'UTF-8'
   }
   ```

4. Mit `.\gradlew build` bauen. Das Jar liegt danach in `build/libs/`.

Zum Testen am besten das gebaute Jar zusammen mit dem Wireless-Redstone-Jar in einen normalen Forge-1.12.2-Server legen. In der Entwicklungsumgebung startet der Mod wegen der Abhängigkeit nicht ohne Wireless Redstone.

---

## 🗂️ Aufbau

| Klasse | Aufgabe |
| --- | --- |
| `NstpMod` | Einstiegspunkt, Start-Reset, Reset-Logik |
| `NstpCommand` | `/nstp`-Befehl mit Tab-Completion |
| `ChatHandler` | Chat abfangen, Schema prüfen, Frequenzen auslösen |
| `SessionManager` | Merkt sich, wer für welches Stellwerk aktiv ist |
| `PulseScheduler` | Schaltet Frequenzen ein und nach `pulseTicks` wieder aus |
| `NstpConfig` | Liest und schreibt `nstp.json` |
| `FrequencyBackend` / `WirelessRedstoneBackend` | Einzige Schnittstelle zu Wireless Redstone |

---

## 🙏 Danksagung

Die Schaltlogik baut auf dem Mod **Wireless Redstone** von [Razzokk](https://github.com/Razzokk/wireless-redstone) auf.
Danke auch an Claude Code, die KI hat eigentlich alles gemacht

*Gute Fahrt und immer freie Fahrt! 🚂*
