## Playwright 101: A quick tutorial for Java acceptance tests

This section provides a **minimal mental model + practical basics** to understand how Playwright works in this project and how it compares with Selenium.

### Core concept

Playwright is a modern **browser automation framework** designed for reliability and cross-browser support. It:

1. launches a real (or headless) browser (Chromium, Firefox, or WebKit)
2. loads your application page
3. interacts with it like a user (typing, clicking)
4. waits for DOM events automatically where possible
5. verifies expected behavior

In short:

> **Playwright tests what the user actually sees and does, with more built-in waiting and assertion helpers than Selenium.**

---

### Java setup (Maven dependency)

Add this dependency to `pom.xml`:

```xml
<dependency>
  <groupId>com.microsoft.playwright</groupId>
  <artifactId>playwright</artifactId>
  <version>1.42.0</version>
  <scope>test</scope>
</dependency>
```

Install browser binaries:

```bash
mvn -q exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

(If you prefer to install manually, run `mvn test` after adding the Java setup code below.)

---

### 1. Browser and Page APIs

```java
try (Playwright playwright = Playwright.create()) {
    Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    Page page = browser.newPage();

    page.navigate("http://localhost:8080/index.html");

    page.fill("#title", "Clean Code");
    page.fill("#author", "Robert C. Martin");
    page.click("#saveBtn");

    page.waitForSelector("#message:has-text('Created book')");
    String message = page.textContent("#message");

    assertTrue(message.contains("Created book"));
}
```

- `Playwright.create()` initializes the driver runtime.
- `browser.newPage()` opens a new tab equivalent.
- `page` is the primary interaction object.

---

### 2. Auto-waits and stability

Playwright has built-in auto-waiting for:

- element actions (`click`, `fill`)
- navigation
- animations and transitions
- network events

In contrast, Selenium usually needs explicit wait code (`WebDriverWait`).

Preferred pattern:

```java
page.click("#saveBtn");
page.waitForSelector("#bookList .book-item");
```

No `Thread.sleep()` required.

---

### 3. Locator usage

```java
Locator bookItems = page.locator("#bookList .book-item");
assertEquals(1, bookItems.count());
assertTrue(bookItems.first().textContent().contains("Clean Code"));
```

This is concise and robust. Playwright resolves locators lazily and retries automatically.

---

### 4. Network interception (optional, advanced)

```java
page.route("**/api/books", route ->
    route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("application/json")
        .setBody("[]")
    )
);
```

Use this to stub backend responses and isolate front-end behavior during acceptance tests.

---

### Minimal end-to-end example

```java
try (Playwright playwright = Playwright.create()) {
    Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    Page page = browser.newPage();

    page.navigate("http://localhost:8080/index.html");
    page.fill("#title", "Refactoring");
    page.fill("#author", "Martin Fowler");
    page.click("#saveBtn");

    page.waitForSelector("#message:has-text('Created book')");

    Locator items = page.locator("#bookList .book-item");
    assertEquals(1, items.count());
    assertTrue(items.first().textContent().contains("Refactoring"));
}
```

---

### Selenium vs Playwright (comparison)

Playwright strengths:

- built-in auto-waiting (less flaky tests)
- one API for Chrome, Firefox, WebKit
- stronger first-class network/mocking capabilities
- built-in test runner in Playwright CLI (for JS/TS), but Java use in JUnit is easy
- modern API with `Page` and `Locator` primitives

Selenium strengths:

- mature ecosystem and broad integrations (TestNG, Selenide, etc.)
- wider existing enterprise support
- good for projects already standardized on Selenium

For new Java acceptance tests, Playwright is often quicker to write and more stable while Selenium remains solid where the team already has a Selenium-based pipeline.

---

### Quick mapping from Selenium to Playwright

- `driver.get(url)` -> `page.navigate(url)`
- `driver.findElement(By.id("x"))` -> `page.locator("#x")`
- `element.click()` -> `page.click("#x")`
- `WebDriverWait` + `ExpectedConditions` -> `page.waitForSelector(...)` (auto-wait by default)

---

### Headless vs headed mode

Default headless:

```java
Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
```

For debugging, use headed:

```java
Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
```

---

### Test lifecycle suggested pattern

1. `@BeforeEach`: clear in-memory repo and set up Playwright engine.
2. `@Test`: open page, interact, assert.
3. `@AfterEach`: close page/browser and cleanup.

This mirrors the Selenium lifecycle with less explicit wait overhead.

