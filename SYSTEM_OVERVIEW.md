# News Recommendation System — Full Overview

## 1. How the System Works (High Level)

```
User launches app
        │
        ▼
┌──────────────────┐     starts background thread
│    Main.java     │ ───────────────────────► ┌───────────────────┐
│  (entry point)   │                          │  LoginServer:9999 │
│                  │                          │  (TCP socket)     │
│ Sets FlatLaf UI  │                          └───────────────────┘
│ Opens LoginFrame │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│   LoginFrame     │  ◄── CardLayout switches between:
│  (Swing window)  │      • Login panel (username + password + login button)
└──────────────────┘      • Register panel (username + password + confirm + register button)
         │
    login success
    (userId > 0)
         │
         ▼
┌──────────────────┐
│  DashboardFrame  │  ◄── JTabbedPane with 3 tabs
│  (Swing window)  │
└──────────────────┘
    ├── Tab 1: "Local Comparison"  ─── Option1Panel
    ├── Tab 2: "Online Comparison" ─── Option2Panel
    └── Tab 3: "Topic Search"      ─── Option3Panel
```

### Three main workflows:

**Local Comparison (Tab 1):** Select a base PDF/DOCX + multiple compare files → TF-IDF compares all against base → shows ranked similarity scores → saves to `analysis_history` → can export CSV.

**Online Comparison (Tab 2):** Upload one PDF/DOCX → extract keywords → crawl Arxiv + ScienceDaily + Wikipedia → TF-IDF compare crawled articles → shows ranked results with clickable links → saves to `analysis_history` → can export CSV.

**Topic Search (Tab 3):** Enter a topic → crawl all sources → filter results matching the topic keyword → display as HTML with clickable links.

---

## 2. Package-by-Package Breakdown

### Package: `org.newsrec`

#### `Main.java`
- **Purpose:** Application entry point
- **`main(String[] args)`**
  1. Starts `LoginServer` in a new background thread (so it doesn't block the UI)
  2. Applies FlatLaf look-and-feel (modern flat UI theme)
  3. Opens `LoginFrame` on the Event Dispatch Thread (EDT) via `SwingUtilities.invokeLater()`

---

### Package: `org.newsrec.ui`

#### `LoginFrame.java`
- **Purpose:** Login/Registration window using `CardLayout` to switch between login and register panels
- **Fields:**
  - `cardLayout` / `mainPanel` — switches between "login" and "register" cards
  - `loginUsername`, `loginPassword` — login form fields
  - `regUsername`, `regPassword`, `regConfirm` — registration form fields
  - `loginBtn`, `regBtn` — action buttons
- **`buildLoginPanel()`** — creates the login card:
  - Username text field + Password field (with Show/Hide toggle)
  - Big Login button (centered, bigger font)
  - "No Account? Register" blue underlined link at bottom
  - Enter key in password field triggers login
- **`buildRegisterPanel()`** — creates the register card:
  - Username, Password, Confirm Password (all with Show/Hide toggles)
  - Big Register button
  - "Already have an account? Login" blue underlined link
  - Enter key in confirm field triggers register
  - Validates: all fields required, passwords must match
- **`wrapPasswordField(JPasswordField)`** — wraps a password field in a JPanel with a Show/Hide toggle button on the right. Clicking toggles `setEchoChar((char)0)` (visible) vs `setEchoChar('•')` (masked)
- **`login()`** — calls `LoginClient.login()` → if userId > 0, disposes login window and opens `DashboardFrame(userId)`
- **`register()`** — calls `LoginClient.register()` → on success, switches back to login card with username pre-filled
- **Network protocol used:** Sends `"LOGIN"` or `"REG"` as first line, then username, then password

#### `DashboardFrame.java`
- **Purpose:** Main application window with 3 tabs
- **Constructor `(int userId)`** — stores userId, creates JTabbedPane:
  - Tab "Local Comparison" → `Option1Panel(userId)`
  - Tab "Online Comparison" → `Option2Panel(userId)`
  - Tab "Topic Search" → `Option3Panel()` (no userId needed)
- **Key:** Passes `userId` through so history saving can associate records with the logged-in user

#### `Option1Panel.java`
- **Purpose:** Local file-to-file comparison
- **Fields:** `baseFile`, `compareFiles` list, `lastResults`, `exportBtn`, `historyDAO`
- **Constructor `(int userId)`** — sets up:
  - Top toolbar: Upload Base File, Remove Base File, Add Compare Files, Remove Selected, Analyze, Export CSV
  - Center: Base file info panel + Compare files list
  - Bottom: Results panel (BoxLayout Y_AXIS) + progress bar
- **`chooseBase()`** — file chooser (PDF/DOCX filter) → sets `baseFile`
- **`chooseFiles()`** — multi-select file chooser → adds to `compareFiles`
- **`analyze()`**
  1. Validates base + compare files selected
  2. Disables buttons, shows progress bar
  3. Spawns `SwingWorker` → calls `RecommendationService.compareFiles(baseFile, compareFiles)`
  4. On `done()`: saves results to `lastResults`, calls `saveHistory()`, calls `displayResults()`, enables Export CSV
- **`displayResults(List<RecommendationResult>)`** — clears results panel, adds a row per result with: file name, similarity score (4 decimal places), "Open in Explorer" button
- **`saveHistory(List<RecommendationResult>)`** — iterates results, calls `historyDAO.save(userId, baseFileName, comparedFileName, score)` for each
- **`exportCsv()`** — file save dialog → `CsvExporter.export(lastResults, file)`
- **`openInExplorer(String path)`** — uses `Runtime.exec(new String[]{"explorer.exe", "/select,", path})` to highlight file
- **`setButtonsEnabled(boolean)`** — manages enabled state of all buttons

#### `Option2Panel.java`
- **Purpose:** Upload file → online crawl → compare
- **Fields:** `userId`, `historyDAO`, `results` (JEditorPane for HTML), `uploadBtn`, `exportBtn`, `progressBar`, `lastResults`
- **Constructor `(int userId)`** — sets up top panel with Upload + Export CSV buttons, progress bar at bottom, JEditorPane in center
- **Upload flow:**
  1. File chooser (PDF/DOCX filter) → select file
  2. SwingWorker: `OnlineRecommendationService.recommend(file, progressCallback)`
  3. `process(List<String>)` — appends progress messages as HTML (inserts before `</body>`)
  4. `done()` — on success: stores in `lastResults`, calls `saveHistory()`, enables Export CSV, renders final HTML results with clickable article links; on error: shows error dialog
- **`exportCsv()`** — same pattern as Option1Panel
- **`saveHistory()`** — saves each result's similarity to `analysis_history` with base name "Online Analysis"

#### `Option3Panel.java`
- **Purpose:** Search crawled sources by topic keyword
- **Fields:** `topicField`, `resultPane` (JEditorPane), `searchBtn`, `progressBar`
- **`search()`**
  1. Validates non-empty topic
  2. Shows progress bar, sets topic text in HTML (with HTML escaping)
  3. SwingWorker: `TopicSearchService.search(topic)` (crawls + filters by keyword match)
  4. `done()` — renders results as HTML with clickable article links; on error, shows error in pane
- Enter key in topic field triggers search (via `ActionListener`)

---

### Package: `org.newsrec.network`

#### `LoginServer.java`
- **Purpose:** TCP socket server on port 9999 that handles login/register requests
- **`start()`** — binds a `ServerSocket` on port 9999, loops accepting connections. Each client gets a new thread via `handleClient()`. Catches `BindException` gracefully.
- **`handleClient(Socket)`** — reads 3 lines from socket: `action` ("LOGIN" or "REG"), `username`, `password`. Dispatches to `AuthService.authenticate()` or `AuthService.register()`. Writes `userId` back (0 = failure).

#### `LoginClient.java`
- **Purpose:** TCP socket client that sends requests to `LoginServer:9999`
- **`sendAction(action, username, password)`** — opens socket, sends 3 lines, parses integer response (0 = failure)
- **`login(username, password)`** — calls `sendAction("LOGIN", ...)`
- **`register(username, password)`** — calls `sendAction("REG", ...)`

---

### Package: `org.newsrec.service`

#### `AuthService.java`
- **Purpose:** Authentication/registration facade
- **`authenticate(username, password)`** → `UserDAO.login()` → returns userId or 0
- **`register(username, password)`** → `UserDAO.register()` → returns new userId or 0

#### `KeywordExtractor.java`
- **Purpose:** Extract top-N most frequent words from text
- **`extractKeywords(text, topN)`**:
  1. Tokenizes via `TextPreprocessor.tokenize()` (lowercase, remove non-alpha, remove stop words)
  2. Counts frequency of each word
  3. Sorts by frequency descending, takes topN
  4. Returns list of keyword strings

#### `OnlineRecommendationService.java`
- **Purpose:** Full pipeline: read file → extract keywords → crawl → vectorize → compare → sort
- **`recommend(File, Consumer<String> progress)`**:
  1. Read file text via `ReaderFactory`
  2. Extract top-5 keywords via `KeywordExtractor`
  3. If no keywords: retry with first 1000 chars; if still none: fallback to "artificial intelligence"
  4. Crawl via `CrawlerManager.collectArticles(keywords, progress)`
  5. Build corpus: uploaded text + each article title+description
  6. Tokenize corpus, precompute IDF vectors
  7. Build TF-IDF vector for base file, then for each article
  8. Compute cosine similarity between base vector and each article vector
  9. Sort results by similarity descending
  10. Return `List<RecommendationResult>`

#### `TopicSearchService.java`
- **Purpose:** Search articles by topic keyword
- **`search(topic)`**:
  1. Crawl all sources via `CrawlerManager.collectArticles(List.of(topic))`
  2. Filter results where title OR description contains the topic (case-insensitive)
  3. Return matching `List<RSSArticle>` (in arbitrary order)

---

### Package: `org.newsrec.dao`

#### `UserDAO.java`
- **Purpose:** Database operations for `users` table
- **`login(username, password)`** — `SELECT id FROM users WHERE username=? AND password_hash=?` → returns userId or 0
- **`register(username, password)`** — `INSERT INTO users (username, password_hash) VALUES (?,?)` with `RETURN_GENERATED_KEYS` → returns new userId or 0

#### `HistoryDAO.java`
- **Purpose:** Save analysis history to `analysis_history` table
- **`save(userId, baseFile, comparedFile, similarity)`** — INSERT with 4 parameters. Uses Log4j for error logging.

---

### Package: `org.newsrec.database`

#### `DBConnection.java`
- **Purpose:** Provide JDBC connections to SQL Server
- **Static initializer:** Loads `application.properties` from classpath (reads `db.url`, `db.user`, `db.password`)
- **`getConnection()`** — calls `DriverManager.getConnection()` with those properties. Wraps errors in `RuntimeException` + logs them.

---

### Package: `org.newsrec.crawler`

#### `RSSArticle.java`
- **Purpose:** Data class for crawled article (title, link, description, source)
- **Fields:** `title`, `link`, `description`, `source` + getters

#### `RSSParser.java`
- **Purpose:** Parse RSS/XML feed into `RSSArticle` list
- **`parse(rssUrl, source)`**:
  1. Opens HTTP connection with 5s connect / 10s read timeouts
  2. Creates hardened `DocumentBuilderFactory` (XXE protection: disables doctypes, external entities)
  3. Parses XML, finds all `<item>` elements
  4. Extracts `<title>`, `<link>`, `<description>` from each item
  5. Returns list of `RSSArticle` objects

#### `ArxivCrawler.java`
- **Purpose:** Crawl arXiv cs.AI RSS feed
- **`crawl()`** — calls `RSSParser.parse("https://export.arxiv.org/rss/cs.AI", "Arxiv")`

#### `ScienceDailyCrawler.java`
- **Purpose:** Crawl ScienceDaily AI RSS feed
- **`crawl()`** — calls `RSSParser.parse("https://www.sciencedaily.com/rss/computers_math/artificial_intelligence.xml", "ScienceDaily")`

#### `WikipediaCrawler.java`
- **Purpose:** Scrape Wikipedia page for a keyword
- **`search(keyword)`**:
  1. Builds URL: `https://en.wikipedia.org/wiki/` + keyword (spaces → underscores)
  2. Uses JSoup to GET the page (5s timeout)
  3. Extracts `<title>` and first `<p>` text
  4. Returns single `RSSArticle` (or empty list on error)

#### `CrawlerManager.java`
- **Purpose:** Orchestrate all crawlers asynchronously with timeouts
- **`collectArticles(keywords)`** — overload that calls the progress version with a no-op consumer
- **`collectArticles(keywords, progress)`**:
  1. Creates synchronized list for thread-safe adds
  2. Launches `CompletableFuture` for Arxiv + ScienceDaily (15s timeout each, 20s combined)
  3. Launches one `CompletableFuture` per keyword for Wikipedia (10s timeout each, 15s combined)
  4. Waits for Arxiv/SciDaily batch first, then Wikipedia batch
  5. Times out gracefully with progress messages
  6. Returns all collected articles (no deduplication)

---

### Package: `org.newsrec.reader`

#### `DocumentReader.java` (Interface)
- **Purpose:** Contract for file readers
- **`read(File)`** — returns file content as String

#### `PDFReader.java`
- **Purpose:** Read PDF files using Apache PDFBox 3.x
- **`read(File)`** — loads PDF via `Loader.loadPDF()`, extracts text via `PDFTextStripper`

#### `DOCXReader.java`
- **Purpose:** Read DOCX files using Apache POI
- **`read(File)`** — opens via `XWPFDocument`, extracts paragraphs joined with `\n` newlines

#### `ReaderFactory.java`
- **Purpose:** Factory that returns the correct reader for a file
- **`getReader(File)`** — checks `.pdf` → `PDFReader`, `.docx` → `DOCXReader`, else throws `IllegalArgumentException`

---

### Package: `org.newsrec.recommendation`

#### `TextPreprocessor.java`
- **Purpose:** Tokenize and clean text
- **`tokenize(String text)`**:
  1. Lowercase
  2. Remove all non-alphabetic / non-space characters (`[^a-zA-Z ]`)
  3. Split on whitespace
  4. Filter out words ≤ 2 characters
  5. Filter out stop words (40 common English stop words)
  6. Return `List<String>` of remaining words

#### `TFIDFVectorizer.java`
- **Purpose:** Build TF-IDF vectors from text
- **`buildVector(document, corpus)`** — tokenizes corpus, precomputes IDF, builds vector for document
- **`buildVector(document, idfMap)`** — tokenizes document, for each unique term: TF = count/total_terms × IDF from map
- **`precomputeIDF(tokenizedCorpus)`** — counts docs containing each term, IDF = `log(totalDocs / (1 + docCount))`
- **`preprocess(text)`** — delegates to `TextPreprocessor.tokenize()` (convenience method)
- **`tf(term, terms)`** — private: builds frequency map in O(n), returns term count / total terms (optimized from O(n²) stream filter)

#### `CosineSimilarity.java`
- **Purpose:** Compute cosine similarity between two TF-IDF vectors
- **`calculate(Map a, Map b)`**:
  1. Iterates `a` keys: dot product, and sum squares for magA
  2. Iterates `b` keys separately: sum squares for magB (correct — covers all terms in both vectors)
  3. Returns `dot / (sqrt(magA) * sqrt(magB))` or 0 if either magnitude is 0

#### `RecommendationService.java`
- **Purpose:** Compare base file against multiple compare files
- **`compareFiles(baseFile, compareFiles)`**:
  1. Read base file text
  2. Read all compare files text (cached in `Map<File, String>` to avoid re-reading)
  3. Build corpus (base + all compare texts)
  4. Build TF-IDF vector for base
  5. For each compare file: build vector, compute cosine similarity against base
  6. Sort all results by similarity descending
  7. Return sorted `List<RecommendationResult>`

---

### Package: `org.newsrec.util`

#### `HtmlUtils.java`
- **Purpose:** Escape strings for safe HTML rendering
- **`escape(String)`** — replaces `&` `<` `>` `"` `'` with HTML entities. Used by Option2Panel and Option3Panel to prevent XSS from crawler content.

#### `BrowserUtil.java`
- **Purpose:** Open a URL in the system browser
- **`open(String url)`** — calls `Desktop.getDesktop().browse(new URI(url))`. Logs errors.

#### `CsvExporter.java`
- **Purpose:** Export recommendations to CSV file
- **`export(List<RecommendationResult>, File)`** — writes header row + data rows with columns: File Name, Source, Similarity, Link, File Path. Escapes double quotes in values.

---

### Package: `org.newsrec.model`

#### `User.java`
- **Purpose:** POJO for user data
- Fields: `id`, `username`, `passwordHash`, `createdAt` (declared but unused)

#### `Article.java`
- **Purpose:** POJO for article data
- Fields: `id`, `title`, `content`, `source`, `category`, `url`

#### `RecommendationResult.java`
- **Purpose:** Result DTO for similarity comparison
- Fields: `fileName`, `filePath`, `similarity`, `source`, `link`
- Multiple constructors for different usage contexts (local files vs online articles)

---

## 3. Database Schema

```sql
users (
    id              INT  PRIMARY KEY IDENTITY,
    username        NVARCHAR(50) UNIQUE NOT NULL,
    password_hash   NVARCHAR(255) NOT NULL,   -- currently stores plaintext
    created_at      DATETIME DEFAULT GETDATE()
)

analysis_history (
    id              INT  PRIMARY KEY IDENTITY,
    user_id         INT  NOT NULL → FK → users(id),
    base_file       NVARCHAR(255),             -- "Online Analysis" for online comparisons
    compared_file   NVARCHAR(255),             -- file name or article title
    similarity      FLOAT,
    created_at      DATETIME DEFAULT GETDATE()
)

-- Also defined but not used in code:
articles (id, title, content, source, category, url, created_at)
vectors  (id, article_id → FK, term, tfidf_score)
```

---

## 4. Data Flow Diagrams

### Local File Comparison

```
User clicks "Analyze"
        │
        ▼
RecommendationService.compareFiles(baseFile, compareFiles)
        │
        ├── ReaderFactory.getReader(baseFile).read(baseFile)     → baseText
        ├── for each compareFile:
        │      ReaderFactory.getReader(file).read(file)          → text (cached)
        │
        ├── Build corpus [baseText, text1, text2, ...]
        │
        ├── TFIDFVectorizer.buildVector(baseText, corpus)
        │       ├── preprocessor.tokenize() each doc
        │       ├── precomputeIDF(tokenizedCorpus)
        │       └── tf(term, terms) × idf(term)  → Map<term, score>
        │
        ├── for each compareFile:
        │       TFIDFVectorizer.buildVector(text, corpus)
        │       CosineSimilarity.calculate(baseVector, compareVector)  → score
        │
        ├── Sort by score descending
        │
        └── Return List<RecommendationResult>
                │
                ▼
        Option1Panel:
          ├── displayResults()      → show in UI
          ├── saveHistory()         → HistoryDAO → analysis_history table
          └── user clicks Export CSV → CsvExporter → .csv file
```

### Online Comparison

```
User clicks "Upload File" → selects file
        │
        ▼
OnlineRecommendationService.recommend(file, progress)
        │
        ├── Read file text
        ├── KeywordExtractor.extractKeywords(text, 5)
        │       └── TextPreprocessor.tokenize() + frequency sort
        │
        ├── CrawlerManager.collectArticles(keywords)
        │       ├── ArxivCrawler.crawl()         → RSSParser → List<RSSArticle>
        │       ├── ScienceDailyCrawler.crawl()   → RSSParser → List<RSSArticle>
        │       └── WikipediaCrawler.search(kw)   → JSoup → RSSArticle
        │
        ├── Build corpus [baseText + each article title+desc]
        ├── TF-IDF vectorize + CosineSimilarity (same as local flow)
        ├── Sort by score descending
        └── Return List<RecommendationResult>
                │
                ▼
        Option2Panel:
          ├── Show results as HTML with clickable links
          ├── saveHistory()
          └── Export CSV button
```

### Login / Registration Flow

```
LoginFrame (UI)
    │
    ▼
LoginClient.login("user", "pass")     or    LoginClient.register("user", "pass")
    │                                             │
    ▼                                             ▼
Opens TCP socket → localhost:9999    →    Opens TCP socket → localhost:9999
Sends: "LOGIN\nuser\npass\n"              Sends: "REG\nuser\npass\n"
    │                                             │
    ▼                                             ▼
LoginServer.handleClient(socket)    →    LoginServer.handleClient(socket)
    │                                             │
    ▼                                             ▼
AuthService.authenticate(u, p)        →    AuthService.register(u, p)
    │                                             │
    ▼                                             ▼
UserDAO.login(u, p)                   →    UserDAO.register(u, p)
SELECT id FROM users                   INSERT INTO users (username, password_hash)
WHERE username=? AND password_hash=?   VALUES (?,?) RETURNING id
    │                                             │
    ▼                                             ▼
Returns userId (0 if fail)            →    Returns new userId (0 if fail)
    │                                             │
    ▼                                             ▼
LoginFrame: if userId > 0             →    LoginFrame: success dialog
  → dispose(), new DashboardFrame(id)      → switch to login card
```

---

## 5. Key Design Decisions

| Decision | Why |
|----------|-----|
| **TCP socket for login** | Simple, no need for HTTP server; but credentials are plaintext over loopback |
| **FlatLaf look-and-feel** | Modern Swing look without custom UI code |
| **SwingWorker in all tabs** | Prevents UI freeze during file reading, crawling, and TF-IDF computation |
| **TF-IDF + Cosine Similarity** | Classic, lightweight text comparison; no ML dependencies needed |
| **CompletableFuture with timeouts for crawling** | Non-blocking parallel fetching; timeouts prevent UI hang on slow feeds |
| **No connection pooling** | Simple app, one user at a time; accept the overhead |
| **XXE-hardened XML parser** | Security: prevents malicious RSS feeds from reading files or making SSRF |
| **HTML escaping in results** | Prevents XSS from crawler content rendered in JEditorPane |
| **CardLayout for login/register** | Single window, no popup dialogs; smooth transition |
| **`analysis_history` writes after analysis** | Persists every comparison so users can review past work |

---

## 6. How to Run

```powershell
mvn compile
mvn exec:java
```

Requires: Java 21, Maven, SQL Server on localhost:1433 with `SmartNewsDB` database.
DB credentials in `src/main/resources/application.properties`.
