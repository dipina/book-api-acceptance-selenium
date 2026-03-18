package com.example.bookdemo;

import com.example.bookdemo.repository.BookRepository;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookPlaywrightAcceptanceTest {

    private static final Logger logger = LoggerFactory.getLogger(BookPlaywrightAcceptanceTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private BookRepository bookRepository;

    private Playwright playwright;
    private Browser browser;
    private BrowserContext browserContext;
    private Page page;

    @BeforeEach
    void setUp() {
        logger.info("Starting test setup for BookPlaywrightAcceptanceTest.");
        logger.info("Clearing repository so the acceptance test starts from a known empty state.");
        bookRepository.clear();

        logger.info("Creating the Playwright engine. This bootstraps the Playwright runtime used to automate browsers.");
        playwright = Playwright.create();

        logger.info("Launching a headless Chromium browser through Playwright.");
        logger.info("Unlike Selenium's WebDriver model, Playwright talks to the browser through its own automation layer and provides built-in auto-waiting.");
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );

        logger.info("Creating an isolated browser context. This gives the test a clean session with separate cookies, storage, and cache.");
        browserContext = browser.newContext();

        logger.info("Opening a new page/tab inside the isolated context.");
        page = browserContext.newPage();

        logger.info("Playwright browser setup completed successfully.");
    }

    @AfterEach
    void tearDown() {
        logger.info("Starting Playwright cleanup.");
        if (browserContext != null) {
            logger.info("Closing browser context and discarding session state created during the test.");
            browserContext.close();
        }
        if (browser != null) {
            logger.info("Closing the browser instance.");
            browser.close();
        }
        if (playwright != null) {
            logger.info("Shutting down the Playwright engine.");
            playwright.close();
        }
        logger.info("Cleanup finished.");
    }

    @Test
    void userCanCreateABookFromHtmlFormWithPlaywright() {
        String url = "http://localhost:" + port + "/index.html";
        logger.info("Navigating to the HTML page under test: {}", url);
        logger.info("Playwright instructs Chromium to load the page exactly as a real user would see it.");
        page.navigate(url);

        logger.info("Locating the title input using a CSS locator: #title");
        Locator titleInput = page.locator("#title");
        logger.info("Filling the title field with test data: Clean Code");
        titleInput.fill("Clean Code");

        logger.info("Locating the author input using a CSS locator: #author");
        Locator authorInput = page.locator("#author");
        logger.info("Filling the author field with test data: Robert C. Martin");
        authorInput.fill("Robert C. Martin");

        logger.info("Locating the save button using a CSS locator: #saveBtn");
        Locator saveButton = page.locator("#saveBtn");
        logger.info("Clicking the save button. This simulates a real user action and triggers the HTML form workflow, which sends POST /api/books to the Spring Boot backend.");
        saveButton.click();

        logger.info("Locating the message area using a CSS locator: #message");
        Locator message = page.locator("#message");
        logger.info("Using Playwright's web-first assertion to wait until the message contains the expected confirmation text.");
        assertThat(message).containsText("Created book #1: Clean Code");

        logger.info("Locating the rendered book list items using CSS selector: #bookList .book-item");
        Locator bookItems = page.locator("#bookList .book-item");

        logger.info("Using Playwright's auto-retrying assertion to wait until exactly one book is rendered in the UI.");
        assertThat(bookItems).hasCount(1);

        logger.info("Reading the first rendered book item after Playwright has confirmed that the list is stable.");
        Locator firstBook = bookItems.first();

        logger.info("Verifying that the first rendered book matches the submitted title and author.");
        assertThat(firstBook).hasText("Clean Code — Robert C. Martin");

        logger.info("Final UI text returned by the first rendered book item: {}", firstBook.textContent());
        logger.info("Acceptance test completed successfully: Playwright filled the form, clicked the button, automatically waited for UI synchronization, and verified the visible result.");
    }
}