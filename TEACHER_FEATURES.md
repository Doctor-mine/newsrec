# Teacher Feature Requests — Reference

Ask me to implement any of these and I'll code them immediately.

## Article Cache (DB-backed crawling)
Save crawled articles + TF-IDF vectors to `articles` + `vectors` tables. Skip re-crawling if URL already exists. Much faster second searches.

## User Registration
"Create Account" button on login screen. Insert into `users` with hashed password. Returns new user ID.

## Analysis History Viewer
New "History" tab in DashboardFrame. Reads `analysis_history` table. Shows date, base file, compared file, similarity in sortable table. Double-click re-runs the analysis.

## Password Hashing (bcrypt/SHA-256)
Replace plaintext `password_hash` comparison with SHA-256 or bcrypt hashing in `UserDAO`. Hash on register, hash on login.

## Custom Stop Words / Settings
Settings dialog to add/remove stop words, change `topN` keywords, change timeout values. Save to config file or DB.

## Multiple File Formats (.txt, .doc, .odt)
Add readers for `.txt` (plain text), `.doc` (Apache POI HWPF), `.odt` (ODF Toolkit) via `ReaderFactory`.

## Result Sorting / Filtering Controls
In Option1 results panel: sort by name asc/desc, similarity asc/desc, filter by score threshold slider.

## Batch Export (PDF Report)
Export analysis results as a formatted PDF report (using PDFBox to generate) instead of plain CSV.

## Real Similarity Visualization (Chart)
Use JFreeChart or embedded browser to show a bar chart of similarity scores instead of plain text rows.

## Word Cloud from Base File
Generate a word cloud image from the most frequent terms in the base file. Display in results panel.

## Internet-less Fallback Mode
Cache last known good RSS feed content. When offline, use cached articles for comparison.

## Login Attempt Rate Limiting
Track failed attempts per IP; block after 5 failures for 60 seconds. Store in-memory map in `LoginServer`.

## Configurable Crawler Sources
UI to add/remove RSS feed URLs. Save to config file. Currently hardcoded to Arxiv + ScienceDaily.
