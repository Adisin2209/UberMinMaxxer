#!/bin/bash

# Nach der gewünschten Distribution fragen
echo ""
echo "== UberEats MinMaxxer Simple Installer =="
echo "!!-- UberEats MinMaxxer requires --Maven, Chromedriver, Chrome, OpenJDK-- !!"
echo ""
echo "Which Linux-Distro are you using?"
echo "0 - Ubuntu"
echo "1 - Arch"
echo ""
read -r distro

# Abhängig von der Eingabe die entsprechende Aktion ausführen
if [ "$distro" -eq 1 ]; then
    
    echo "Installing dependencies for Arch Linux..."
    
    # Überprüfen, ob yay installiert ist
    if ! command -v yay &> /dev/null; then
        echo "yay is not installed. Installing yay..."
        
        # yay installieren
        sudo pacman -S --needed git base-devel
        git clone https://aur.archlinux.org/yay.git
        cd yay || exit
        makepkg -si --noconfirm
        cd .. || exit
        rm -rf yay
    else
        echo "yay is already installed."
    fi
    
    # OpenJDK installieren
    echo "Installing OpenJDK..."
    sudo pacman -S --needed jdk-openjdk
    
    # Maven installieren
    sudo pacman -S --needed maven
    
    # Chrome und Chromedriver installieren
    echo "Installing Google Chrome and Chromedriver..."
    yay -S --needed chromedriver google-chrome

elif [ "$distro" -eq 0 ]; then
    echo "Installing dependencies for Ubuntu..."
    
    # System-Updates
    sudo apt update
    
    # OpenJDK installieren
    echo "Installing OpenJDK..."
    sudo apt install -y openjdk-21-jdk
    
    # Maven installieren
    sudo apt install -y maven wget unzip
    
    # Google Chrome installieren
    echo "Installing Google Chrome..."
    wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
    sudo dpkg -i google-chrome-stable_current_amd64.deb || sudo apt-get -f install -y
    rm google-chrome-stable_current_amd64.deb
    
    # Chromedriver installieren
    echo "Installing Chromedriver..."
    wget https://chromedriver.storage.googleapis.com/92.0.4515.107/chromedriver_linux64.zip
    unzip chromedriver_linux64.zip
    sudo mv chromedriver /sbin/chromedriver
    sudo chown root:root /sbin/chromedriver
    sudo chmod +x /sbin/chromedriver

else
    echo "Ungültige Eingabe. Bitte 1 oder 0 eingeben."
    exit 1
fi

# Überprüfen, ob Java korrekt installiert wurde
echo "Checking Java installation..."
java -version
if [ $? -ne 0 ]; then
    echo "Java installation failed. Please check your system and try again."
    exit 1
fi

# mvn package clean ausführen
echo "Running 'mvn clean package'..."
mvn clean package

echo "Installation complete."

