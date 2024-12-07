#!/bin/bash

# Git-Repository-URL (ersetzen mit deinem Repository-URL)
REPO_URL="https://github.com/dein-benutzername/dein-repository.git"

# Pfad zum lokalen Git-Repository
REPO_PATH="."

# Überprüfen, ob die JAR-Datei existiert
JAR_PATH="target/Uber_Minmaxxer-1.0-SNAPSHOT-jar-with-dependencies.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo "Fehler: Die Datei $JAR_PATH wurde nicht gefunden. Bitte stellen Sie sicher, dass der Maven-Build erfolgreich war."
    exit 1
fi

# Prüfen, ob lokale Änderungen vorliegen
if [ "$(git -C $REPO_PATH status --porcelain)" ]; then
    echo "Es gibt lokale Änderungen. Bitte committe oder stash deine Änderungen vor dem Update."
    exit 1
fi

# Prüfen, ob lokale und Remote-Version übereinstimmen
echo "Überprüfe auf Updates..."
git -C $REPO_PATH fetch origin

LOCAL_COMMIT=$(git -C $REPO_PATH rev-parse HEAD)
REMOTE_COMMIT=$(git -C $REPO_PATH rev-parse origin/main)

if [ "$LOCAL_COMMIT" != "$REMOTE_COMMIT" ]; then
    echo "Neue Version verfügbar. Update jetzt? [y/N]"
    read -r UPDATE_CHOICE
    if [[ "$UPDATE_CHOICE" =~ ^[Yy]$ ]]; then
        echo "Hole die neuesten Änderungen..."
        git -C $REPO_PATH pull origin main
        echo "Update abgeschlossen."
    else
        echo "Update übersprungen. Starte mit der aktuellen Version."
    fi
else
    echo "Deine Version ist auf dem neuesten Stand."
fi

# Java-Befehl ausführen
echo "Starte Uber_Minmaxxer..."
java -jar "$JAR_PATH"

