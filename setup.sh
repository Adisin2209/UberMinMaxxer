#!/bin/bash

# Nach der gewünschten Distribution fragen
echo ""
echo "==UberEats MinMaxxer simple Installer=="
echo "!!--UberEats MinMaxxer requires --Maven, Chromedriver, Chrome-- --!!"
echo ""
echo "Which Linux-Distro are you using?"
echo "0 - Ubuntu"
echo "1 - Arch"
echo ""
read -r distro

# Abhängig von der Eingabe die entsprechende Aktion ausführen
if [ "$distro" -eq 1 ]; then
    echo "Installing dependancies"
    sudo pacman -S maven
    yay -S chromedriver google-chrome
    
elif [ "$distro" -eq 0 ]; then
    echo "Installing dependancies"
    
    # Maven installieren
    sudo apt update
    sudo apt install -y maven wget unzip
    
    # Google Chrome installieren
    echo "Installing Google Chrome..."
    wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
    sudo dpkg -i google-chrome-stable_current_amd64.deb || sudo apt-get -f install -y
    rm google-chrome-stable_current_amd64.deb
    
    # Chromedriver installieren
    echo "Installing Chromedriver..."
    CHROME_VERSION=$(google-chrome --version | grep -oE '[0-9.]+' | head -n 1)
    CHROMEDRIVER_VERSION=$(curl -s "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_$CHROME_VERSION")
    wget "https://chromedriver.storage.googleapis.com/$CHROMEDRIVER_VERSION/chromedriver_linux64.zip"
    unzip chromedriver_linux64.zip
    sudo mv chromedriver /usr/local/bin/
    chmod +x /usr/local/bin/chromedriver
    rm chromedriver_linux64.zip
    
else
    echo "Ungültige Eingabe. Bitte 1 oder 0 eingeben."
fi
