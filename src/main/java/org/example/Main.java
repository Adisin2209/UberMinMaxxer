package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.example.Settings;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.example.Helpers.*;

public class Main {

    //region Variables

    public static float VERSION = 1.3f;

    public static WebDriver driver;
    public static String cUrl;
    public static volatile boolean isScraping = true;
    public static int speedUp = 2;

    public static int foundOffers = 0;

    public static class Site {
        String storeName;
        String storeLink;

        public Site(String storeName, String storeLink) {
            this.storeName = storeName;
            this.storeLink = storeLink;
        }
    }

    public static class Article{
        String title;
        String Price;
        public Article(String title, String Price) {
            this.title = title;
            this.Price = Price;
        }
    }
    public static List<Site> scrapedSites = new ArrayList<>();
    //endregion


    public static void main(String[] args) {
        clearConsole();

        System.out.println(title);
        System.out.println("Version: " + VERSION +"v");

        // Input
        Scanner usrInput = new Scanner(System.in);

        System.out.println("Uber Eats URL zum Scrapen angeben: ");
        System.out.printf("["+Colors.GREEN+ "INPUT"+Colors.RESET +"]: ");
        cUrl = usrInput.nextLine();


        // Init
        initialize();

        // Scrape
        scrape();

        // Verarbeite alle gesammelten Seiten

        startAnimationThread("Products in Store",2);

        scrapeAllCollectedSites();


        stopAnimationThread();

    }

    public static void initialize() {
        Logger seleniumLogger = Logger.getLogger("org.openqa.selenium");
        seleniumLogger.setLevel(Level.SEVERE);


       // System.setProperty("webdriver.chrome.driver", "/sbin/chromedriver");
        driver = new ChromeDriver();
    }

    public static void scrape() {
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
            driver.quit();
        }
    }

    public static void scrapeAllCollectedSites() {
        // Reinitialisiere den Driver für die nächste Phase
        initialize();

        for (Site site : scrapedSites) {
            scrapeStorePage(site.storeName, site.storeLink);
        }


        driver.quit();
    }
    public static void scrapeStorePage(String storeName, String storeLink) {
        try {
            driver.get(storeLink);

            List<Article> articles = new ArrayList<>();

            JavascriptExecutor js = (JavascriptExecutor) driver;
            boolean endReached = false;

            // Scroll to the bottom of the page
            while (!endReached) {
                long lastHeight = (long) js.executeScript("return document.body.scrollHeight");
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(2000/speedUp);
                long newHeight = (long) js.executeScript("return document.body.scrollHeight");

                if (newHeight == lastHeight) {
                    endReached = true;
                }
            }

            List<WebElement> storeItems = driver.findElements(By.cssSelector("li[data-testid^='store-item']"));

            boolean storePrinted = false;

            for (WebElement storeItem : storeItems) {
                try {
                    boolean hasOffer = false;

                    List<WebElement> subDivs = storeItem.findElements(By.tagName("div"));
                    for (WebElement subDiv : subDivs) {
                        if (subDiv.getText().contains("Kaufe 1 und erhalte 1 kostenlos")) {
                            hasOffer = true;
                            break;
                        }
                    }

                    if (hasOffer) {

                        if (!storePrinted) {
                            stopAnimationThread();
                            System.out.println("\u200E");
                            System.out.println("=============================================================================");
                            System.out.println("Store Name: " + storeName);
                            System.out.println("Link: " + storeLink);
                          //  Helpers.startAnimationThread("Products in Store", 2);
                            storePrinted = true;
                        }

                        List<WebElement> richTextElements = storeItem.findElements(By.cssSelector("[data-testid='rich-text']"));

                        if (richTextElements.size() >= 2) {
                            Article article = new Article(richTextElements.get(0).getText(), richTextElements.get(1).getText());

                            boolean isDuplicate = false;
                            for (Article article1 : articles) {
                                if (article.title.equals(article1.title) && article.Price.equals(article1.Price)) {
                                    isDuplicate = true;
                                    break;
                                }
                            }

                            if (!isDuplicate) {
                                articles.add(article);
                                stopAnimationThread();

                                String priceString = article.Price;
                                priceString = priceString.replace("€", "").trim();
                                priceString = priceString.replace(",", ".");
                                float price = Float.parseFloat(priceString);

                                System.out.println(">" + article.title + " " + article.Price + " => " + price/2+"/Person");
                                startAnimationThread("Products in NEXT Store", 2);
                            }
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Fehler beim Verarbeiten eines Store-Items: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Fehler beim Scrapen der Store-Seite: " + e.getMessage());
        }
    }


}
