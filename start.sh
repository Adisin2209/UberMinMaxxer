#!/bin/bash

# Überprüfen, ob die JAR-Datei existiert
JAR_PATH="target/Uber_Minmaxxer-1.0-SNAPSHOT-jar-with-dependencies.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo "Fehler: Die Datei $JAR_PATH wurde nicht gefunden. Bitte stellen Sie sicher, dass der Maven-Build erfolgreich war."
    exit 1
fi

# Java-Befehl ausführen
echo "Starte Uber_Minmaxxer..."
java -jar "$JAR_PATH"
