#!/bin/bash

# Hole den absoluten Pfad des aktuellen Verzeichnisses
CURRENT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Füge den Ordner zum PATH hinzu, falls er noch nicht enthalten ist
if [[ ":$PATH:" != *":$CURRENT_DIR:"* ]]; then
    echo "export PATH=\$PATH:$CURRENT_DIR" >> ~/.zshrc
    echo "Der Ordner $CURRENT_DIR wurde erfolgreich zum PATH hinzugefügt."
else
    echo "Der Ordner $CURRENT_DIR ist bereits im PATH."
fi

# Erstelle einen Symlink für 'umm', falls er noch nicht existiert
if [[ ! -e "$CURRENT_DIR/umm" ]]; then
    ln -s "$CURRENT_DIR/start.sh" "$CURRENT_DIR/umm"
    echo "Symlink 'umm' wurde erstellt und zeigt auf start.sh."
else
    echo "Der Symlink 'umm' existiert bereits."
fi

echo "Starte ein neues Terminal oder führe 'source ~/.zshrc' aus, damit die Änderungen wirksam werden."

