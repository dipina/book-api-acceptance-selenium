package com.example.bookdemo;

import com.example.bookdemo.repository.BookRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookAcceptanceTest {

    private static final Logger logger = LoggerFactory.getLogger(BookAcceptanceTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private BookRepository bookRepository;

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        logger.info("Starting test setup for BookAcceptanceTest.");
        logger.info("Clearing repository so the acceptance test starts from a known empty state.");
        bookRepository.clear();

        logger.info("Resolving and configuring the ChromeDriver binary with WebDriverManager.");
        WebDriverManager.chromedriver().setup();

        logger.info("Creating ChromeOptions for a stable automated browser session.");
        ChromeOptions options = new ChromeOptions();
        logger.info("Enabling headless mode so the browser runs without opening a visible window.");
        options.addArguments("--headless=new");
        logger.info("Adding --no-sandbox for compatibility in restricted/containerized environments.");
        options.addArguments("--no-sandbox");
        logger.info("Adding --disable-dev-shm-usage to reduce shared-memory issues in CI/container environments.");
        options.addArguments("--disable-dev-shm-usage");

        logger.info("Launching a new ChromeDriver instance. Selenium will now control a real browser.");
        driver = new ChromeDriver(options);
        logger.info("Browser started successfully and is ready for UI interaction.");
    }

    @AfterEach
    void tearDown() {
        logger.info("Starting test cleanup.");
        if (driver != null) {
            logger.info("Closing the browser and releasing Selenium/WebDriver resources.");
            driver.quit();
        }
        logger.info("Cleanup finished.");
    }

    @Test
    void userCanCreateABookFromHtmlForm() {
        String url = "http://localhost:" + port + "/index.html";
        logger.info("Navigating to the HTML page under test: {}", url);
        logger.info("Selenium instructs the browser to load the page so the test can interact with the UI exactly as a user would.");
        driver.get(url);

        logger.info("Locating the title input field by id='title'.");
        WebElement titleInput = driver.findElement(By.id("title"));
        logger.info("Entering the book title into the form: Clean Code");
        titleInput.sendKeys("Clean Code");

        logger.info("Locating the author input field by id='author'.");
        WebElement authorInput = driver.findElement(By.id("author"));
        logger.info("Entering the author name into the form: Robert C. Martin");
        authorInput.sendKeys("Robert C. Martin");

        logger.info("Locating the save button by id='saveBtn'.");
        WebElement saveButton = driver.findElement(By.id("saveBtn"));
        logger.info("Clicking the save button. This simulates a real user action and should trigger the form submission logic, which sends a POST request to the Spring Boot backend.");
        saveButton.click();

        logger.info("Creating an explicit wait with a timeout of 10 seconds.");
        logger.info("Selenium will now wait for asynchronous UI updates instead of asserting immediately.");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        logger.info("Waiting for the confirmation message element with id='message' to become visible.");
        WebElement message = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("message"))
        );

        logger.info("Confirmation message is now visible. Text returned by the UI: {}", message.getText());
        logger.info("Verifying that the UI confirms the creation of book #1 with the expected title.");
        assertTrue(message.getText().contains("Created book #1: Clean Code"));

        logger.info("Waiting for the rendered book list to be updated in the DOM.");
        logger.info("Selenium repeatedly checks the elements matching CSS selector '#bookList .book-item' until the first item contains the expected text.");
        wait.until(driver -> {
            List<WebElement> items = driver.findElements(By.cssSelector("#bookList .book-item"));
            return !items.isEmpty() && items.get(0).getText().contains("Clean Code — Robert C. Martin");
        });

        logger.info("Book list update detected. Fetching the rendered list items from the page.");
        List<WebElement> items = driver.findElements(By.cssSelector("#bookList .book-item"));
        logger.info("Number of book items currently displayed in the UI: {}", items.size());
        if (!items.isEmpty()) {
            logger.info("First rendered book item text: {}", items.get(0).getText());
        }

        logger.info("Asserting that exactly one book is displayed.");
        assertEquals(1, items.size());

        logger.info("Asserting that the displayed book entry matches the submitted title and author.");
        assertEquals("Clean Code — Robert C. Martin", items.get(0).getText());

        logger.info("Acceptance test completed successfully: Selenium filled the form, submitted it, waited for the backend-driven UI update, and verified the visible result.");
    }
}