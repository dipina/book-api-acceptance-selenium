# Spring Boot REST + HTML Form + Selenium Acceptance Test Demo

This Maven project is a complete, minimal example of an acceptance test for a Spring Boot application with:

- a REST API implemented with Spring Boot
- a static HTML user interface served by the same app
- JavaScript in the page that sends a `POST` request to the REST API
- a Selenium-based acceptance test that drives a real browser and verifies the visible outcome

The demo use case is intentionally simple: a user fills in a form to create a book, the browser sends `POST /api/books`, and the page updates the list of books.

## Project structure

```text
book-demo/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/example/bookdemo/
│   │   │   ├── BookDemoApplication.java
│   │   │   ├── model/
│   │   │   │   ├── Book.java
│   │   │   │   └── CreateBookRequest.java
│   │   │   ├── repository/
│   │   │   │   └── BookRepository.java
│   │   │   └── web/
│   │   │       └── BookController.java
│   │   └── resources/static/
│   │       └── index.html
│   └── test/
│       └── java/com/example/bookdemo/
│           └── BookAcceptanceTest.java
```

## Functional overview

### REST endpoints

The application exposes two endpoints:

- `POST /api/books` creates a new book from JSON:

```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin"
}
```

- `GET /api/books` returns the current in-memory list of books.

### HTML interface

The HTML page is available at:

- `http://localhost:8080/index.html`

The page includes:

- a title input
- an author input
- a submit button
- a message area
- a list of created books

The browser-side JavaScript does this:

1. intercepts the form submission
2. sends a JSON `POST` request to `/api/books`
3. displays a success or error message
4. reloads the visible list of books from `GET /api/books`

## Requirements

You need the following installed locally.

### Required

- Java 17 or newer
- Maven 3.9+ recommended
- Google Chrome installed

### Helpful to verify versions

```bash
java -version
mvn -version
google-chrome --version
```

On macOS, Chrome may be installed as an application bundle; the executable version command can differ.

## How to compile the project

From the project root:

```bash
mvn clean compile
```

This compiles the main application classes.

## How to launch the application

From the project root:

```bash
mvn spring-boot:run
```

Once started, open:

```text
http://localhost:8080/index.html
```

You can then manually test the flow:

1. type a title
2. type an author
3. click **Create book**
4. verify the success message appears
5. verify the new book appears in the list

## How to package the app

```bash
mvn clean package
```

This creates a runnable JAR in the `target/` directory.

Run it with:

```bash
java -jar target/book-demo-0.0.1-SNAPSHOT.jar
```

## How to run the acceptance test

```bash
mvn test
```

The Selenium test starts the Spring Boot app on a random port, launches Chrome in headless mode, opens the HTML page, fills the form, clicks the button, and verifies that the created book is shown in the page.

## Selenium details

### Why Selenium is used here

Selenium is appropriate when you want to validate the actual browser-level user journey:

- the HTML is rendered by a browser
- JavaScript is executed for real
- the click and form submission are simulated like a user interaction
- the final assertion is based on what the user would see on screen

That makes this a stronger acceptance-level test than a plain controller or API test.

### What the test does exactly

The included `BookAcceptanceTest` performs the following steps:

1. starts the Spring Boot application with `@SpringBootTest(webEnvironment = RANDOM_PORT)`
2. clears the in-memory repository before each test
3. configures Chrome in headless mode
4. opens `/index.html`
5. enters a title and author
6. submits the form
7. waits until the success message is visible
8. waits until the book list contains the newly created item
9. asserts the expected text in the UI

### Browser driver management

The test uses **WebDriverManager** to resolve and configure `chromedriver` automatically:

```java
WebDriverManager.chromedriver().setup();
```

This avoids the older manual process of:

- downloading a specific `chromedriver`
- placing it in your `PATH`
- keeping it in sync with your installed Chrome version

### Common Selenium execution issues

If `mvn test` fails, the most common causes are:

#### 1. Chrome is not installed

Install Google Chrome and rerun the tests.

#### 2. Chrome version and driver mismatch

WebDriverManager usually handles this, but in restricted corporate environments or offline environments it can fail.

#### 3. CI or Linux container sandbox restrictions

The test already adds:

- `--headless=new`
- `--no-sandbox`
- `--disable-dev-shm-usage`

Those flags are commonly needed in containers and CI pipelines.

#### 4. No GUI available

That is fine here because the test uses headless Chrome.

### Running a visible browser instead of headless

To watch the test run, edit `BookAcceptanceTest.java` and remove or comment out:

```java
options.addArguments("--headless=new");
```

Then rerun:

```bash
mvn test
```

A Chrome window should open during the test.

## Why this is an acceptance test and not just a unit or integration test

This project combines multiple layers in a single test flow:

- browser UI
- client-side JavaScript
- HTTP POST request
- Spring Boot REST controller
- server-side state update
- HTTP GET request to refresh data
- final visible UI output

That end-to-end path is what makes it a practical acceptance test example.

## Alternative testing approaches

Selenium is one option, not the only option.

### Java alternatives

#### 1. Playwright for Java

Playwright is often easier and more reliable than Selenium for modern web apps. It has powerful auto-waiting and strong tooling.

Typical reasons to prefer it:

- simpler synchronization in dynamic pages
- modern browser automation API
- good trace/debug support

#### 2. Selenide

Selenide is a Java wrapper around Selenium that reduces boilerplate and makes tests more concise.

Typical reasons to prefer it:

- shorter and cleaner test code
- built-in waiting abstractions
- easier maintenance for UI tests

#### 3. Spring MockMvc or WebTestClient

These are useful, but they are not browser automation tools. They are excellent for testing the server side, especially controllers and HTTP contracts.

Typical reasons to prefer them:

- fast feedback
- no browser dependency
- precise server-side assertions

But they do **not** validate real browser rendering or JavaScript execution.

### Python alternatives

If you want the acceptance test outside Java, there are strong Python options.

#### 1. Playwright for Python

A very strong modern choice for browser automation.

Typical advantages:

- clean API
- robust waiting behavior
- good support for Chromium, Firefox, and WebKit
- strong CI ergonomics

A minimal Python ecosystem setup would look like:

```bash
python -m venv .venv
source .venv/bin/activate
pip install playwright pytest
playwright install
```

#### 2. Selenium for Python

The Python Selenium bindings are mature and widely used.

Typical setup:

```bash
python -m venv .venv
source .venv/bin/activate
pip install selenium pytest webdriver-manager
```

This is a good fit when:

- the system under test is written in Java but the QA stack is Python
- the team already has Python-based automation

#### 3. Robot Framework

Robot Framework can sit on top of Selenium or Playwright and is useful when the team wants keyword-driven tests.

Typical reasons to prefer it:

- readable test cases
- good for mixed technical/non-technical collaboration
- strong plugin ecosystem

## Example of what a Python alternative would test

Even if the implementation language changes, the acceptance scenario is the same:

1. open the HTML page
2. fill in the title field
3. fill in the author field
4. click the submit button
5. wait for the success message
6. verify the created book appears in the list

That scenario is tool-agnostic; only the browser automation library changes.

## Suggested next improvements

If you want to evolve this demo into something closer to production, typical next steps would be:

- replace the in-memory repository with Spring Data JPA and H2/PostgreSQL
- add server-side error handling with structured JSON errors
- add client-side validation messages
- split acceptance tests from fast integration tests
- run the browser tests in CI
- add a second acceptance test for validation failures

## Troubleshooting

### Port already in use

The manual app launch uses port 8080 by default. Stop the existing app or override the port:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Maven cannot resolve dependencies

Check network access to Maven Central and your proxy configuration.

### Acceptance test hangs

This is usually caused by browser startup issues or environment restrictions. Re-run with visible Chrome and inspect logs.

## License / usage

This project is intended as a learning and demo template. Adapt it freely to your own Spring Boot application.
