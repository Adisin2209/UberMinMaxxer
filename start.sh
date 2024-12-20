#!/bin/bash

# Ermittle das Verzeichnis, in dem sich dieses Skript befindet
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Git-Repository-URL
REPO_URL="https://github.com/Adisin2209/UberMinMaxxer.git"

# Lokaler Pfad zum Git-Repository
REPO_PATH="$SCRIPT_DIR"

# Pfade zur JAR-Datei und zum CleanStart-Skript
JAR_PATH="$SCRIPT_DIR/target/Uber_Minmaxxer-1.0-SNAPSHOT-jar-with-dependencies.jar"
CLEAN_START_SCRIPT="$SCRIPT_DIR/cleanstart.sh"

# Überprüfen, ob die JAR-Datei existiert
if [ ! -f "$JAR_PATH" ]; then
    echo "Fehler: Die Datei $JAR_PATH wurde nicht gefunden."
    if [ -f "$CLEAN_START_SCRIPT" ]; then
        echo "Starte CleanStart-Skript..."
        bash "$CLEAN_START_SCRIPT"
    else
        echo "Fehler: CleanStart-Skript $CLEAN_START_SCRIPT wurde nicht gefunden."
    fi
    exit 1
fi

# Prüfen, ob lokale und Remote-Version übereinstimmen
echo "Überprüfe auf Updates..."
git -C "$REPO_PATH" fetch origin

LOCAL_COMMIT=$(git -C "$REPO_PATH" rev-parse HEAD)
REMOTE_COMMIT=$(git -C "$REPO_PATH" rev-parse origin/main)

if [ "$LOCAL_COMMIT" != "$REMOTE_COMMIT" ]; then
    echo "Neue Version verfügbar. Update jetzt? [y/N]"
    read -r UPDATE_CHOICE
    if [[ "$UPDATE_CHOICE" =~ ^[Yy]$ ]]; then
        echo "Hole die neuesten Änderungen..."
        git -C "$REPO_PATH" pull origin main
        echo "Update abgeschlossen."

        # Starte CleanStart-Skript nach dem Update
        if [ -f "$CLEAN_START_SCRIPT" ]; then
            echo "Starte CleanStart-Skript..."
            bash "$CLEAN_START_SCRIPT"
            exit 0
        else
            echo "Fehler: CleanStart-Skript $CLEAN_START_SCRIPT wurde nicht gefunden."
            exit 1
        fi
    else
        echo "Update übersprungen. Starte mit der aktuellen Version."
    fi
else
    echo "Deine Version ist auf dem neuesten Stand."
fi

# Starte Java-Anwendung
echo "Starte Uber_Minmaxxer..."
java -jar "$JAR_PATH"

