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
    
    # Überprüfen, ob pip installiert ist
    if ! command -v pip &> /dev/null; then
        echo "pip is not installed. Do you want to install pip? [Y/n]"
        read -r install_pip
        if [[ "$install_pip" =~ ^[Yy]$ || -z "$install_pip" ]]; then
            sudo apt install -y python3-pip
        else
            echo "pip installation skipped. Chromedriver installation might fail."
        fi
    else
        echo "pip is already installed."
    fi
    
    # Google Chrome installieren
    echo "Installing Google Chrome..."
    wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
    sudo dpkg -i google-chrome-stable_current_amd64.deb || sudo apt-get -f install -y
    rm google-chrome-stable_current_amd64.deb
    
    # Chromedriver installieren
    echo "Installing Chromedriver..."
    pip install webdriver-manager --break-system-packages

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

# Fragen, ob 'umm' zum PATH hinzugefügt werden soll
echo ""
echo "Do you want to add 'umm' (UberMinmaxxer) to path? [Y/n]"
read -r add_to_path

if [[ "$add_to_path" =~ ^[Yy]$ || -z "$add_to_path" ]]; then
    echo "Adding 'umm' to path..."
    ./addtopath.sh
else
    echo "'umm' was not added to path."
fi

echo "Installation complete."

