## Selenium 101: A quick tutorial for Java acceptance tests

This section provides a **minimal mental model + practical basics** to understand how Selenium works in this project and how to write your own acceptance tests.

### Core concept

Selenium is a **browser automation tool**. Instead of testing your backend directly, it:

1. launches a real browser (Chrome in this case)  
2. loads your application page  
3. interacts with it like a user (typing, clicking)  
4. reads what is rendered on screen  
5. verifies expected behavior  

In short:

> **Selenium tests what the user actually sees and does.**

---

### Key building blocks in Selenium (Java)

#### 1. WebDriver

`WebDriver` is the main interface used to control the browser.

```java
WebDriver driver = new ChromeDriver();
```

Think of it as:
> “a remote control for the browser”

---

#### 2. Navigating to a page

```java
driver.get("http://localhost:8080/index.html");
```

This tells the browser to load your application just like a user typing a URL.

---

#### 3. Locating elements

Selenium finds elements using **locators**:

```java
driver.findElement(By.id("title"));
driver.findElement(By.cssSelector("#bookList .book-item"));
```

Common strategies:

- `By.id(...)` → fastest and most reliable  
- `By.cssSelector(...)` → flexible and powerful  
- `By.xpath(...)` → powerful but harder to maintain  

Best practice:
> Prefer `id` or stable CSS selectors.

---

#### 4. Interacting with the page

Once an element is found, you can simulate user actions:

```java
element.sendKeys("Clean Code");   // typing
element.click();                  // clicking
```

This is equivalent to a real user filling and submitting a form.

---

#### 5. Waiting for asynchronous behavior

Modern web apps are asynchronous (AJAX / fetch). You must **wait** for UI updates.

```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

WebElement message = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.id("message"))
);
```

Why this matters:

- without waits → flaky tests  
- with waits → stable, reliable tests  

Rule of thumb:
> Never assume the UI updates instantly.

---

#### 6. Assertions (verification)

After interaction, you verify what the user sees:

```java
assertTrue(message.getText().contains("Created book"));
```

This is the **acceptance criteria** encoded as code.

---

### Minimal end-to-end example

This is the exact pattern used in this project:

```java
driver.get("/index.html");

driver.findElement(By.id("title")).sendKeys("Clean Code");
driver.findElement(By.id("author")).sendKeys("Robert C. Martin");
driver.findElement(By.id("saveBtn")).click();

WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

WebElement message = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.id("message"))
);

assertTrue(message.getText().contains("Clean Code"));
```

---

### Test lifecycle in this project

Each test follows a clear structure:

1. **Setup**
   - clear repository
   - start browser

2. **Exercise**
   - open page
   - interact with UI

3. **Verify**
   - wait for UI update
   - assert expected result

4. **Cleanup**
   - close browser

This is a classic **Given–When–Then** flow:

- Given → empty system  
- When → user submits form  
- Then → book appears in UI  

---

### Headless vs visible execution

#### Headless (used by default)

```java
options.addArguments("--headless=new");
```

- faster  
- required for CI/CD  
- no browser window  

#### Visible mode (for debugging)

Remove the headless option:

```java
// options.addArguments("--headless=new");
```

Then rerun:

```bash
mvn test
```

A Chrome window will open so you can observe the test.

---

### Good practices for Selenium tests

#### 1. Use stable selectors

Prefer:

```java
By.id("saveBtn")
```

Avoid brittle selectors like:

```java
By.xpath("//div[3]/button[2]")
```

---

#### 2. Always use waits

Never do:

```java
Thread.sleep(2000); // ❌ bad practice
```

Always use:

```java
WebDriverWait + ExpectedConditions
```

---

#### 3. Keep tests focused on user behavior

Test this:

- “user creates a book and sees it in the list”

Avoid testing:

- internal method calls  
- repository state directly  

---

#### 4. Run few acceptance tests, not many

Selenium tests are:

- slower  
- heavier  

Use them for **critical user journeys**, not every edge case.

---

### When to use Selenium vs other tools

Use Selenium when:

- you need **real browser validation**  
- JavaScript behavior matters  
- UI correctness is critical  

Do NOT use Selenium when:

- testing pure REST APIs → use MockMvc / REST Assured  
- testing business logic → use unit tests  

---

### Summary

Selenium allows you to test your system at the **highest level of realism**:

- real browser  
- real HTTP calls  
- real rendering  
- real user interactions  

In this project, it validates the full flow:

> HTML form → JavaScript → REST API → backend → UI update → visible result

That is exactly what makes it an **acceptance test**.
