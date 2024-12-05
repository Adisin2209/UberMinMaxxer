package org.example;

import static org.example.Main.isScraping;

public class Helpers {

    private static Thread animation; // Shared animation thread

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
}
