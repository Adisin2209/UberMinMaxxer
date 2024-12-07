package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.example.Helpers.*;

public class Main {

    //region Variables

    public static float VERSION = 1.6f;

    public static WebDriver driver;
    public static String cUrl;
    public static volatile boolean isScraping = true;
    public static int speedUp = 3;
    public static int foundOffers = 0;

    public static class Site {
        String storeName;
        String storeLink;

        public Site(String storeName, String storeLink) {
            this.storeName = storeName;
            this.storeLink = storeLink;
        }
    }

    public static class Article {
        String title;
        String Price;

        public Article(String title, String Price) {
            this.title = title;
            this.Price = Price;
        }
    }

    public static List<Site> scrapedSites = new ArrayList<>();
    public static Scanner usrInput = new Scanner(System.in); // Globaler Scanner

    //endregion

    public static void main(String[] args) {
        clearConsole();
        System.out.println("Welcome");
        System.out.println(title);
        System.out.println("Version: " + Colors.GREEN_BOLD_BRIGHT + VERSION + "v" + Colors.RESET);
        System.out.println();
        // Init
        initialize();

        // Input
        initialInput();

        scrape();

        // Verarbeite alle gesammelten Seiten
        startAnimationThread("Products in Store", 2);
        scrapeAllCollectedSites();
        stopAnimationThread();

        driver.quit();
        end();

    }

    public static void end() {
        System.out.println(Colors.PURPLE_BOLD_BRIGHT+"\n[FINISHED"+Colors.RESET+"]");
        System.out.println("Do another Scrap? [Y/N]");
        System.out.printf("[" + Colors.GREEN + "INPUT" + Colors.RESET + "]: ");
        String input = usrInput.nextLine();
        //usrInput.nextLine(); // Puffer leeren
        if(input.equalsIgnoreCase("Y")) {
            clearConsole();
            System.out.println("Welcome");
            System.out.println(title);
            System.out.println("Version: " + Colors.GREEN_BOLD_BRIGHT + VERSION + "v" + Colors.RESET);
            System.out.println();
            // Init
            initialize();

            // Input
            initialInput();

            scrape();

            // Verarbeite alle gesammelten Seiten
            startAnimationThread("Products in Store", 2);
            scrapeAllCollectedSites();
            stopAnimationThread();

            driver.quit();
        }else {
            System.out.println("Bye :D");
        }

    }

    public static void initialInput() {
        fetchLinks();
        System.out.println("0 - Scrap custom URL");
        System.out.println("1 - Scrap preset");
        System.out.println("2 - Settings");
        System.out.printf("[" + Colors.GREEN + "INPUT" + Colors.RESET + "]: ");
        String input = usrInput.nextLine();

        if (input.equals("0")) {
            deleteLastLines(4);
            getCustomUrl();
        } else if (input.equals("1")) {
            deleteLastLines(4);
            pickPreset();
        } else if (input.equals("2")) {
            deleteLastLines(4);
            SettingsMenu();
        } else {
            System.out.println("Invalid Option\n");
            initialInput(); // Rekursiver Aufruf bei ungültiger Eingabe
        }
    }

    public static void settingsAdd() {
        deleteLastLines(4);

        String inputN;
        String inputL;

        System.out.printf("[" + Colors.GREEN + "INPUT(NAME)" + Colors.RESET + "]: ");
        inputN = usrInput.nextLine();

        System.out.printf("[" + Colors.GREEN + "INPUT(URL)" + Colors.RESET + "]: ");
        inputL = usrInput.nextLine();
        AddPreset(inputN, inputL);
        initialInput();
    }

    public static void SettingsMenu() {
        System.out.println("0 - Add Preset");
        System.out.println("1 - Remove Preset");
        System.out.println("2 - View Presets");
        System.out.printf("[" + Colors.GREEN + "INPUT" + Colors.RESET + "]: ");
        int input = usrInput.nextInt();
        usrInput.nextLine(); // Puffer leeren

        if (input == 0) {
            settingsAdd();
        } else if (input == 1) {
            deleteLastLines(4);
            printLinks();
            System.out.println(Colors.RED_BOLD_BRIGHT + "Which Preset do you want to delete?" + Colors.RESET);
            System.out.printf("[" + Colors.GREEN + "INPUT(INDEX)" + Colors.RESET + "]: ");
            int index = usrInput.nextInt();
            usrInput.nextLine(); // Puffer leeren
            removePreset(index);
            initialInput();
        } else if (input == 2) {
            deleteLastLines(4);
            printLinks();
            initialInput();
        } else {
            System.out.println("Invalid Option\n");
            initialInput();
        }
    }

    public static void getCustomUrl() {
        System.out.println("Uber Eats URL to scrap: ");
        System.out.printf("[" + Colors.GREEN + "INPUT" + Colors.RESET + "]: ");
        cUrl = usrInput.nextLine();
    }

    public static void pickPreset() {
        fetchLinks();
        if (linksWithNames.isEmpty()) {
            System.out.println("No presets found. Please add one.");
            return;
        }

        System.out.println("Pick a saved location: ");
        printLinks();
        System.out.printf("[" + Colors.GREEN + "INPUT" + Colors.RESET + "]: ");
        int input = usrInput.nextInt();
        usrInput.nextLine(); // Puffer leeren

        List<Map.Entry<String, String>> entryList = new ArrayList<>(linksWithNames.entrySet());

        if (input >= 0 && input < entryList.size()) {
            Map.Entry<String, String> entry = entryList.get(input);
            cUrl = entry.getValue();
            System.out.println("Selected URL: " + cUrl); // Debug
        } else {
            System.out.println("Invalid Option");
            pickPreset();
        }
    }

    public static void initialize() {
        fetchLinks();
        Logger seleniumLogger = Logger.getLogger("org.openqa.selenium");
        seleniumLogger.setLevel(Level.SEVERE);
    }

    public static void scrape() {
        driver = new ChromeDriver();
        try {
            System.out.println();
            startAnimationThread("Stores",1);
            //  System.out.println(">Scraping started\n");
            driver.get(cUrl);

            JavascriptExecutor js = (JavascriptExecutor) driver;
            boolean endReached = false;

            while (!endReached) {
                try {
                    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5/((int)speedUp)));
                    WebElement moreButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//button[contains(text(), 'Mehr anzeigen')]")
                    ));
                    if (moreButton.isDisplayed()) {
                        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", moreButton);
                        moreButton.click();

                        if(Settings.Debug)
                            System.out.println("Mehr anzeigen Button geklickt...");

                        Thread.sleep(2000/speedUp);
                        continue;
                    }
                } catch (Exception e) {
                    if(Settings.Debug)
                        System.out.println("Kein 'Mehr anzeigen'-Button gefunden.");

                }
                if(Settings.Debug)
                    System.out.println("Kein Button gefunden. Scrolling to the bottom of the page...");

                long lastHeight = (long) js.executeScript("return document.body.scrollHeight");
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(2000/speedUp);
                long newHeight = (long) js.executeScript("return document.body.scrollHeight");

                if (newHeight == lastHeight) {
                    endReached = true;
                }
            }

            if(Settings.Debug)
                System.out.println("<===============Collecting Stores===============>\n");

            List<WebElement> storeCards = driver.findElements(By.cssSelector("div[data-testid=store-card]"));

            for (WebElement storeCard : storeCards) {
                try {

                    if (!storeCard.findElements(By.tagName("figcaption")).isEmpty()) {
                        if (Settings.Debug)
                            System.out.println("Skipping store with <figcaption>: " + storeCard.getText());
                        continue;
                    }

                    String storeName = storeCard.findElement(By.tagName("h3")).getText();
                    String storeLink = storeCard.findElement(By.tagName("a")).getAttribute("href");
                    boolean hasOffer = false;

                    List<WebElement> subDivs = storeCard.findElements(By.tagName("div"));
                    for (WebElement subDiv : subDivs) {
                        if (subDiv.getText().contains("Kaufe 1 und") || subDiv.getText().contains("verfügbare Angebote")) {
                            hasOffer = true;
                            break;
                        }
                    }

                    if (hasOffer) {
                        foundOffers++;
                        scrapedSites.add(new Site(storeName, storeLink));
                        if(Settings.Debug){
                            System.out.println("");
                            System.out.println("Store Name: " + storeName);
                            System.out.println("Link: " + storeLink);
                            System.out.println("");
                        }

                    }
                } catch (Exception e) {
                    System.out.println("Fehler beim Verarbeiten eines Store-Cards: " + e.getMessage());
                }
            }

            //////////
            stopAnimationThread();

            System.out.println();
            System.out.println("<===============RESULTS===============>");
            System.out.println("Total Stores found: " + storeCards.size());
            System.out.println("Total Stores with offer: " + scrapedSites.size());
            System.out.println("<=====================================>");
            // System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //   driver.quit();
            // System.out.println("SUCCESSSS1");
        }
    }

    public static void scrapeAllCollectedSites() {
        for (Site site : scrapedSites) {
            scrapeStorePage(site.storeName, site.storeLink);
        }
    }

    public static void scrapeStorePage(String storeName, String storeLink) {
        try {
            driver.get(storeLink);

            List<Article> articles = new ArrayList<>();
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Scroll bis zum Seitenende
            for (int i = 0; i < 10; i++) { // Maximal 10 Scrolls
                long lastHeight = (long) js.executeScript("return document.body.scrollHeight");
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(3000); // Wartezeit für das Laden von Inhalten
                long newHeight = (long) js.executeScript("return document.body.scrollHeight");

                if (newHeight == lastHeight) {
                    //ystem.out.println("Ende der Seite erreicht.");
                    break;
                }
            }

            // Store-Items holen
            List<WebElement> storeItems = driver.findElements(By.cssSelector("li[data-testid^='store-item']"));
            if (storeItems.isEmpty()) {
                System.out.println("Keine Artikel im Store gefunden.");
                return;
            }

            boolean storePrinted = false;

            for (WebElement storeItem : storeItems) {
                try {
                    // Prüfen, ob das Angebot existiert
                    boolean hasOffer = storeItem.getText().contains("Kaufe 1 und erhalte 1 kostenlos");
                    if (hasOffer) {
                        if (!storePrinted) {
                            stopAnimationThread();
                            System.out.println("\u200E");
                            // Store-Header nur einmal ausgeben
                            System.out.println("=============================================");
                            System.out.println("Name: " + storeName);
                            System.out.println("Link: " + storeLink);
                            storePrinted = true;
                        }

                        // Artikelinformationen extrahieren
                        List<WebElement> richTextElements = storeItem.findElements(By.cssSelector("[data-testid='rich-text']"));
                        if (richTextElements.size() >= 2) {
                            String title = richTextElements.get(0).getText();
                            String price = richTextElements.get(1).getText().replace("€", "").replace(",", ".").trim();

                            // Duplikate prüfen
                            if (articles.stream().noneMatch(a -> a.title.equals(title) && a.Price.equals(price))) {
                                stopAnimationThread();
                                articles.add(new Article(title, price));
                                System.out.println("> " + title + " " + price + "€");
                                startAnimationThread("Products in NEXT Store", 2);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Fehler beim Verarbeiten eines Artikels: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Fehler beim Scrapen der Store-Seite: " + e.getMessage());
        }
    }


}
