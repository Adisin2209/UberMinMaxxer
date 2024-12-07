package org.example;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.*;
import java.util.Map;

import static org.example.Main.isScraping;

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


    public static void fetchLinks(){
        String filePath = "links.txt";

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

        // Map zum Speichern von Namen und Links
        //Map<String, String> linksWithNames = new HashMap<>();

        // Datei einlesen
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String name = null;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("Name:")) {
                    // Namen extrahieren
                    name = line.substring(5).trim(); // "Name:" entfernen und trimmen
                } else if (!line.isEmpty() && name != null) {
                    // Link speichern, wenn Name vorhanden ist
                    linksWithNames.put(name, line);
                    name = null; // Name zurücksetzen
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Datei: " + e.getMessage());
        }

        // Links ausgeben


    }

    public static void printLinks(){

        int i = 0;
        for (Map.Entry<String, String> entry : linksWithNames.entrySet()) {
            System.out.println("Name: " + Colors.GREEN_BOLD+entry.getKey()+Colors.RESET);
            System.out.println("Link: " + entry.getValue());
            System.out.println("Index: " + i);
            i++;
            System.out.println();
        }
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
