package org.example;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.*;

import static org.example.Main.isScraping;
import static org.example.Main.settingsAdd;

public class Helpers {

    private static Thread animation; // Shared animation thread
    public static Map<String, String> linksWithNames = new HashMap<>();


    public static Thread animationThread(String x, int param) {
        return new Thread(() -> {
            try {
                // ANSI Escape Codes to control cursor
                final String SAVE_CURSOR_POSITION = "\u001B[s";
                final String RESTORE_CURSOR_POSITION = "\u001B[u";
                final String MOVE_CURSOR_TO_BOTTOM = "\u001B[999B";
                final String CLEAR_LINE = "\u001B[2K";

                String[] frames = {
                        Colors.YELLOW + "[ Scraping for " + x + ". Please wait.   ]"+Colors.RESET,
                        Colors.YELLOW + "[ Scraping for " + x + ". Please wait..  ]"+Colors.RESET,
                        Colors.YELLOW + "[ Scraping for " + x + ". Please wait... ]"+Colors.RESET,
                };
                int frameIndex = 0;

                // Save the cursor position
                System.out.print(SAVE_CURSOR_POSITION);

                while (isScraping) {
                    // Move cursor to the bottom, clear the line, and print animation frame
                    System.out.print(MOVE_CURSOR_TO_BOTTOM + CLEAR_LINE + "\r" + frames[frameIndex]);
                    frameIndex = (frameIndex + 1) % frames.length;
                    Thread.sleep(500); // Delay between frames
                }

                if (param == 1) {
                    System.out.print(MOVE_CURSOR_TO_BOTTOM + CLEAR_LINE + Colors.GREEN_BOLD+"\rScraping for " + x + " finished!\n"+Colors.RESET);
                }else if (param == 2) {
                    System.out.printf(MOVE_CURSOR_TO_BOTTOM + CLEAR_LINE + "\r");
                }else if (param == 3) {
                    System.out.printf(MOVE_CURSOR_TO_BOTTOM + CLEAR_LINE + "\r");
                }

                // Restore the original cursor position after stopping
                System.out.print(RESTORE_CURSOR_POSITION);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public static void startAnimationThread(String scrapingTarget, int param) {
        isScraping = true; // Ensure scraping flag is true
        animation = Helpers.animationThread(scrapingTarget, param); // Initialize the thread
        //System.out.println();
        animation.start(); // Start the thread
    }

    public static void stopAnimationThread() {
        isScraping = false; // Stop the animation loop
        try {
            if (animation != null) {
                animation.join(); // Wait for the animation thread to fully stop
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        animation = null; // Reset animation thread
      //  System.out.println(); // Optional: Clear line after stopping
    }

    public static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (final Exception e) {
            // Handle any exceptions.
            e.printStackTrace();
        }
    }

    public static void fetchLinks() {
        String filePath = "links.txt";

        // Map zurücksetzen
        linksWithNames.clear();

        // Prüfen, ob die Datei existiert
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("No links.txt found. Generating...");
            try {
                if (file.createNewFile()) {
                    // Standardinhalte hinzufügen
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                        writer.write("Name: Charlottenburg - Insitut Elektrotechnik Neubau\n");
                        writer.write("https://www.ubereats.com/de/feed?diningMode=DELIVERY&pl=JTdCJTIyYWRkcmVzcyUyMiUzQSUyMkVpbnN0ZWludWZlciUyMDE3JTIyJTJDJTIycmVmZXJlbmNlJTIyJTNBJTIyZmZmNDM2NmEtMThjZS05ZWM5LTQwMDItNjA3NDI2NjgzYThiJTIyJTJDJTIycmVmZXJlbmNlVHlwZSUyMiUzQSUyMnViZXJfcGxhY2VzJTIyJTJDJTIybGF0aXR1ZGUlMjIlM0E1Mi41MTU1MSUyQyUyMmxvbmdpdHVkZSUyMiUzQTEzLjMyNjU4JTdE\n\n");
                    }
                    System.out.println("links.txt has been created with default content.");
                }
            } catch (IOException e) {
                System.err.println("Error creating links.txt: " + e.getMessage());
                return;
            }
        }

        // Datei einlesen und Map befüllen
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String name = null;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("Name:")) {
                    name = line.substring(5).trim(); // "Name:" entfernen und trimmen
                } else if (!line.isEmpty() && name != null) {
                    linksWithNames.put(name, line);
                    name = null; // Name zurücksetzen
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Datei: " + e.getMessage());
        }
    }

    public static void AddPreset(String locationName, String link) {
        String filePath = "links.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            // Format der neuen Zeilen
            writer.write("Name: " + locationName + "\n");
            writer.write(link + "\n");
            writer.newLine(); // Fügt eine Leerzeile hinzu
            System.out.println("Preset added: " + locationName);
        } catch (IOException e) {
            System.err.println("Fehler beim Hinzufügen des Presets: " + e.getMessage());
        }

        fetchLinks();
        // Optional: Map aktualisieren, damit der neue Eintrag direkt verfügbar ist
        linksWithNames.put(locationName, link);
    }

    public static void printLinks(){
        fetchLinks();
        if(!linksWithNames.isEmpty()) {
            System.out.println("<========================================");
            int i = 0;
            for (Map.Entry<String, String> entry : linksWithNames.entrySet()) {
                System.out.println();
                System.out.println("Name: " + Colors.GREEN_BOLD + entry.getKey() + Colors.RESET);
                System.out.println("Link: " + entry.getValue());
                System.out.println("Index: " + i);
                i++;
                System.out.println();
            }
            System.out.println("========================================>");
        }else{
            noPresetsMessage();
        }
    }

    public static void noPresetsMessage(){
        System.out.println(Colors.RED+"You have no saved presets. Create one?"+Colors.YELLOW+" [Y/n]"+Colors.RESET);
        Scanner usrInput = new Scanner(System.in);
        System.out.printf("["+Colors.GREEN+ "INPUT"+Colors.RESET +"]: ");
        String inp = usrInput.nextLine();
        if(inp.equalsIgnoreCase("y")){
            settingsAdd();

        }
    }

    public static void removePreset(int index) {
        String filePath = "links.txt";
        List<String> fileContent = new ArrayList<>();
        int currentIndex = 0;

        // Datei einlesen
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String name = null;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("Name:")) {
                    // Namen extrahieren
                    name = line;
                } else if (!line.isEmpty() && name != null) {
                    // Füge Name und Link hinzu, wenn es nicht der zu löschende Index ist
                    if (currentIndex != index) {
                        fileContent.add(name); // Name hinzufügen
                        fileContent.add(line); // Link hinzufügen
                    }
                    name = null; // Name zurücksetzen
                    currentIndex++; // Index erhöhen
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Datei: " + e.getMessage());
            return;
        }

        // Datei mit aktualisierten Einträgen neu schreiben
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
            for (String contentLine : fileContent) {
                writer.write(contentLine);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Schreiben der Datei: " + e.getMessage());
        }

        // Aktualisiere die Map nach dem Löschen
        fetchLinks();

        System.out.println("Preset mit Index " + index + " wurde entfernt.");
        printLinks();
    }


    public static void deleteLastLines(int numLines) {
        for (int i = 0; i < numLines; i++) {
            // Move cursor up one line and clear the line
            System.out.print("\033[F"); // Move cursor up
            System.out.print("\033[2K"); // Clear the entire line
        }
    }

    public static String title = "██╗   ██╗██████╗ ███████╗██████╗     ██████╗     ███████╗ ██████╗ ██████╗      ██╗\n" +
            "██║   ██║██╔══██╗██╔════╝██╔══██╗    ╚════██╗    ██╔════╝██╔═══██╗██╔══██╗    ███║\n" +
            "██║   ██║██████╔╝█████╗  ██████╔╝     █████╔╝    █████╗  ██║   ██║██████╔╝    ╚██║\n" +
            "██║   ██║██╔══██╗██╔══╝  ██╔══██╗    ██╔═══╝     ██╔══╝  ██║   ██║██╔══██╗     ██║\n" +
            "╚██████╔╝██████╔╝███████╗██║  ██║    ███████╗    ██║     ╚██████╔╝██║  ██║     ██║\n" +
            " ╚═════╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝    ╚══════╝    ╚═╝      ╚═════╝ ╚═╝  ╚═╝     ╚═╝\n" +
            "                                                                                  \n" +
            " ██████╗██████╗  █████╗ ██╗    ██╗██╗     ███████╗██████╗                         \n" +
            "██╔════╝██╔══██╗██╔══██╗██║    ██║██║     ██╔════╝██╔══██╗                        \n" +
            "██║     ██████╔╝███████║██║ █╗ ██║██║     █████╗  ██████╔╝                        \n" +
            "██║     ██╔══██╗██╔══██║██║███╗██║██║     ██╔══╝  ██╔══██╗                        \n" +
            "╚██████╗██║  ██║██║  ██║╚███╔███╔╝███████╗███████╗██║  ██║                        \n" +
            " ╚═════╝╚═╝  ╚═╝╚═╝  ╚═╝ ╚══╝╚══╝ ╚══════╝╚══════╝╚═╝  ╚═╝                        \n" +
            "                                                                                  ";
}
