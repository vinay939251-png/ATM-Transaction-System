# MASTER PROMPT: ATM TRANSACTION SYSTEM — DBMS COURSE-END PROJECT

## 1. Role and Objective

Act as a senior software engineer, database architect, Java developer, DBMS instructor, technical documentation specialist, and LaTeX/Word formatting expert.

Build a complete, working, beginner-friendly **ATM Transaction System** as a DBMS course-end project. Produce the application source code, database schema, SQL scripts, testing documentation, project journal, and a complete project report.

I will provide two reference files:

1. **`Course End Report Latex Template(1).zip`** — the official report template. This is the primary authority for report structure, formatting, layout, section order, page design, and LaTeX requirements.
2. **`Fitness_Center_DBMS_Report_edited.pdf`** — a sample DBMS project report. Use it to understand the expected level of detail, presentation style, explanations, and academic depth. Do not copy its fitness-center content into this project.

You must inspect both files before developing the report. Extract and examine the ZIP contents, including its `.tex`, `.cls`, `.sty`, bibliography, image, and other relevant files where present. Read the sample PDF and identify its useful structural conventions.

**Important priority rule:** The official LaTeX template takes precedence over the sample report whenever their formatting or structure differs. Do not redesign the template, invent a different chapter sequence, or omit required sections without a clear reason grounded in the provided materials.

## 2. Project Title

**ATM Transaction System**

Create a realistic academic project demonstrating database design, SQL operations, transaction processing, data integrity, constraints, normalization, and reliable handling of financial transactions.

The system is an educational simulation. It must not connect to real banking networks, real payment gateways, or actual bank accounts.

## 3. Technology Selection

First inspect the course requirements and template. Determine whether the project is expected to use a particular language or database.

If no restriction is stated, use this recommended stack:

- **Java:** application logic and user interface.
- **MySQL:** relational database.
- **JDBC:** connectivity between Java and MySQL.
- **Swing:** desktop graphical user interface, if a GUI is appropriate.
- **SQL:** schema creation, queries, constraints, transaction processing, and database reports.
- **Maven or a simple documented Java project structure:** dependency and build management, depending on the development environment.

Do not introduce unnecessary frameworks, web technologies, cloud services, or extra programming languages. Keep the project suitable for a DBMS course-end submission and easy to demonstrate locally.

If the course specifically requires SQL-only implementation, another language, or a different database, follow that requirement instead of blindly using the recommended stack.

Document the final technology choices and explain why each technology is used.

## 4. Functional Requirements

Implement the following features as a coherent, working application.

### 4.1 Account and Card Management

- Create sample customer records.
- Create bank account records linked to customers.
- Associate ATM cards with accounts.
- Store a card number or masked card identifier, expiry information if required, and card status.
- Support card states such as active, blocked, and expired where appropriate.
- Associate a securely stored PIN verification value with the relevant card.
- Never store plaintext PINs.
- Use synthetic data only.

For a simple academic project, document any simplifications to real-world banking practices.

### 4.2 Authentication

- Ask the user to enter an ATM card identifier and PIN.
- Validate credentials against the database using a safe verification process.
- Restrict repeated failed attempts.
- Temporarily block or disable authentication after the configured failure threshold.
- Allow an administrator to reset a simulated card's status through an authorized operation.
- Do not reveal whether a particular card number exists when authentication fails.

Use prepared statements for SQL operations. Never construct SQL by concatenating untrusted input.

### 4.3 Balance Inquiry

- Display the authenticated account's available simulated balance.
- Do not allow one customer to view another customer's account.
- Retrieve balances from the database rather than relying on a value stored only in application memory.
- Show understandable messages when an operation fails.

### 4.4 Cash Withdrawal

- Allow the user to enter a withdrawal amount.
- Validate that the amount is positive and follows the configured denomination or withdrawal rules.
- Reject transactions exceeding the available balance.
- Support a configurable per-transaction withdrawal limit.
- Prevent simultaneous transactions from overdrawing an account.
- Record every successful withdrawal in the transaction history.
- Record declined attempts only if the documented design explicitly supports them.

Use database transactions and suitable locking or conditional updates to protect account balances.

### 4.5 Cash Deposit

If the project models deposits as an ATM-supported operation, implement deposits as simulated transactions.

- Validate the amount.
- Update the account balance safely.
- Record the deposit in the transaction history.
- Ensure that the balance update and transaction record either both succeed or both fail.

Clearly distinguish a simulated deposit from a real physical ATM cash-acceptance process.

### 4.6 Fund Transfer

Implement an optional transfer between two accounts within the simulated system.

- Validate the source and destination accounts.
- Reject transfers to the same account.
- Reject invalid amounts and transfers exceeding the available balance.
- Debit the source and credit the destination atomically.
- Record both sides of the transfer or use a clearly documented transfer model.
- Assign a shared transfer reference to records belonging to the same transfer.
- Roll back the complete operation if any required step fails.

If fund transfer is outside the course scope, explain that decision and retain withdrawal, deposit, balance inquiry, and transaction history as the core features.

### 4.7 Mini Statement and Transaction History

Display recent transactions for the authenticated account.

Each transaction should include appropriate fields such as:

- Transaction reference.
- Transaction type.
- Amount.
- Date and time.
- Transaction status.
- Balance after the transaction, if supported by the design.
- Related transfer reference, where applicable.

Order transactions consistently, normally newest first. Prevent users from viewing other accounts' transaction histories.

### 4.8 Administrative Functions

Provide a simple authorized administrative workflow, appropriate to the project's scope, to:

- View customer and account summaries.
- Create or manage simulated accounts.
- View transaction history.
- Block or reactivate cards.
- Inspect transaction outcomes.

Protect administrative functions with separate authentication and authorization. Do not give every ATM user administrative privileges.

### 4.9 Input Validation and Error Handling

Handle invalid amounts, missing fields, incorrect credentials, nonexistent records, blocked cards, insufficient balances, duplicate records, database connection failures, and transaction failures.

Display clear messages without exposing passwords, PINs, database credentials, or raw SQL errors.

## 5. Database Design

Design the relational database before implementing the application.

Use a database name such as `atm_transaction_system`, unless the official requirements specify another name.

Create a normalized schema with appropriately named tables. A possible starting design includes:

1. `customers`
2. `accounts`
3. `atm_cards`
4. `transactions`
5. `transfers`, if transfers are implemented
6. `admins`, if administrative authentication is implemented
7. `login_attempts` or an equivalent authentication-attempt mechanism, if required by the design

This list is a starting point, not a command to create redundant tables. Inspect relationships and normalize the schema before finalizing it.

For every table, document:

- Purpose.
- Attributes.
- Primary key.
- Foreign keys.
- Candidate or unique keys where appropriate.
- Data types.
- Nullability.
- Constraints.
- Relationships to other tables.

Use suitable monetary data types such as `DECIMAL`, not floating-point types, for stored financial amounts.

Use foreign keys, `NOT NULL`, `UNIQUE`, `CHECK` constraints where supported, and appropriate indexes.

Prevent negative account balances through a combination of constraints where feasible, atomic conditional updates, and transaction logic.

Document the normalization process, including functional dependencies and the normal forms actually achieved. Do not claim that the database satisfies a normal form without showing the reasoning.

### Required database deliverables

Create:

- `schema.sql` — database and table creation.
- `sample_data.sql` — clearly synthetic demonstration records.
- `queries.sql` — useful DBMS queries and reports.
- Any required stored procedures, triggers, or views, if they genuinely contribute to the project.
- An ER diagram source or diagram.
- A relational schema.
- A data dictionary.
- A description of keys and relationships.

Do not add triggers or stored procedures merely to make the project look more advanced. Explain their purpose if included.

## 6. Transaction Correctness and Data Integrity

Database consistency is one of the project's most important learning objectives.

Implement operations using appropriate database transaction boundaries.

For withdrawals, deposits, and transfers:

1. Validate the request.
2. Begin a database transaction.
3. Verify relevant account and card conditions.
4. Perform safe balance updates.
5. Insert the corresponding transaction records.
6. Commit only if all required operations succeed.
7. Roll back if an operation fails.

Use JDBC transaction controls such as `setAutoCommit(false)`, `commit()`, and `rollback()` where Java and JDBC are used.

Choose appropriate locking or conditional-update techniques for concurrent withdrawals and transfers. Explain why the chosen method prevents overdrafts or partial transfers.

Do not claim that a system is fully concurrency-safe without demonstrating how concurrent operations are handled.

Clearly explain the ACID properties in relation to the implementation:

- Atomicity.
- Consistency.
- Isolation.
- Durability.

If transaction isolation or locking settings are changed, document them and explain the reason.

## 7. Security Requirements

Implement reasonable educational safeguards:

- Prepared statements for user-supplied database values.
- Secure PIN verification rather than plaintext storage.
- Configurable authentication-attempt limits.
- Separation of ATM-user and administrator permissions.
- Validation of account ownership.
- No hardcoded real credentials.
- No real customer or banking information.
- No PINs, passwords, or database secrets in logs.
- A configuration example file containing placeholders rather than actual secrets.
- A `.gitignore` that excludes local credentials and build artifacts.

Include setup instructions explaining how to configure the local database credentials.

Do not claim that this academic project is production-ready or certified for use by financial institutions.

## 8. User Interface

If Swing is suitable and permitted, build a clean Java Swing interface with screens such as:

1. Welcome/login screen.
2. ATM main menu.
3. Balance inquiry.
4. Withdrawal.
5. Deposit.
6. Fund transfer, if implemented.
7. Mini statement.
8. Logout and session reset.
9. A separate administrative interface.

Keep the interface understandable and consistent. Provide input validation and useful status messages.

The interface must call the application/service layer, which performs database operations. Avoid placing all SQL and business logic directly inside button event handlers.

If a GUI is not required by the course, a well-structured console interface is acceptable. Follow the academic requirements rather than adding a GUI at the cost of correctness.

## 9. Software Architecture

Use a clear, maintainable structure. For Java, a suitable organization is:

- `model` — entities and data models.
- `dao` — database access operations.
- `service` — business rules and transaction workflows.
- `ui` — Swing screens or console menus.
- `util` or `config` — database connection and configuration.
- `exception` — custom exceptions, if useful.

Separate SQL access from business rules and presentation.

Use descriptive names, sensible methods, comments for non-obvious logic, and consistent formatting.

Avoid unnecessary abstraction, empty classes, duplicated logic, placeholder methods, and code that appears implemented but does nothing.

## 10. Testing

Create and execute a meaningful test plan.

Include at least the following scenarios:

- Valid login.
- Invalid PIN.
- Repeated failed logins.
- Balance inquiry.
- Successful withdrawal.
- Withdrawal exceeding the balance.
- Invalid or zero amount.
- Successful deposit.
- Successful transfer, if implemented.
- Transfer rollback when a required step fails.
- Access to another customer's account.
- Access to another customer's transaction history.
- Blocked-card authentication.
- Database connection failure.
- Concurrent withdrawal attempts, if the environment permits a reproducible test.

For each test, document:

- Test ID.
- Objective.
- Preconditions.
- Input.
- Expected result.
- Actual result.
- Pass/fail status.
- Evidence, where available.

Actually run the tests that the environment supports. Never fabricate execution results, screenshots, or successful test statuses.

If a test cannot be executed, label it as not executed and provide the reason.

## 11. Strict Report Requirements

Read the official LaTeX template before writing the report.

Preserve the template's required chapter order, title page, institutional details, page layout, heading styles, margins, fonts, spacing, headers, footers, page numbering, tables, diagrams, and bibliography conventions to the extent specified by the provided files.

Inspect the sample Fitness Center DBMS report to understand the expected academic depth, but replace all project-specific content with the ATM Transaction System.

Include all sections required by the official template. Depending on the actual template, these may include:

- Title page.
- Certificate or approval page.
- Declaration.
- Acknowledgement.
- Abstract.
- Table of contents.
- Introduction.
- Problem statement.
- Objectives.
- Scope.
- Existing and proposed systems.
- Requirements analysis.
- Feasibility study.
- System design.
- ER diagram.
- Relational schema.
- Database normalization.
- Implementation details.
- SQL queries.
- Screenshots.
- Testing.
- Results and discussion.
- Limitations.
- Future enhancements.
- Conclusion.
- References or bibliography.
- Appendices.

**This list is conditional.** The actual template determines which sections are required and their order. Do not insert, delete, or reorder chapters merely because they appear in this suggested list.

Write original, technically accurate explanations in a natural academic style. Avoid repetitive filler, generic AI-generated claims, and unsupported assertions.

Use the correct project title consistently throughout the report.

Never invent student names, roll numbers, supervisor names, institutional information, approvals, signatures, test results, references, or project statistics. Preserve legitimate placeholders from the template where details are not supplied.

Use consistent terminology for tables, fields, transactions, and diagrams throughout the report.

## 12. Deliver Two Report Formats

Generate both:

### A. LaTeX report

- Use the supplied LaTeX template and its original structure.
- Produce a complete `.tex` report and all necessary supporting files.
- Retain the template's formatting commands and dependencies wherever possible.
- Include the diagrams and tables in the correct sections.
- Ensure paths to images, bibliography files, and other assets are valid.
- Compile the report if a LaTeX compiler is available.
- Fix compilation errors and missing references where possible.
- Do not claim successful compilation unless it has been verified.

### B. Editable Microsoft Word report

Generate a complete `.docx` version of the same report.

The Word document must be genuinely editable, with text, tables, headings, captions, and diagrams where practical. Do not simply embed every report page as an image.

Preserve the official template's structure and formatting as closely as Word permits, including:

- Title page.
- Required preliminary pages.
- Heading hierarchy.
- Page breaks.
- Page numbering.
- Headers and footers.
- Tables and captions.
- Diagrams.
- References.
- Appendices.

If an exact LaTeX-to-Word layout match is impossible, preserve the document's structure and academic formatting, and document the differences.

Compare the Word report with the LaTeX report to ensure that they contain the same substantive content.

Do not leave unfinished placeholders such as `[INSERT ABSTRACT]`, `TODO`, or `[ADD SCREENSHOT]` unless the template requires a student-supplied field or the required information is unavailable. Clearly identify any unavoidable student-supplied fields.

## 13. Project Journal

Maintain a `README.md` throughout development as a chronological project journal.

Record:

- Project goals and scope.
- Initial template and sample-report inspection.
- Technology selection and rationale.
- Database design decisions.
- Schema and normalization decisions.
- Application architecture.
- Features implemented.
- Files created or modified.
- Commands used to run the application.
- Tests executed and their actual outcomes.
- Bugs encountered and how they were resolved.
- Limitations and unfinished work.
- Instructions for building and demonstrating the project.

Update the journal as work progresses. Do not write a retrospective journal that falsely suggests every step happened earlier than it did.

## 14. Repository Structure

Organize the finished deliverables clearly. Use a structure similar to the following, adapting it to the actual template and project:

```text
ATM-Transaction-System/
├── README.md
├── .gitignore
├── pom.xml
├── src/
│   ├── main/
│   │   └── java/
│   └── test/
├── database/
│   ├── schema.sql
│   ├── sample_data.sql
│   └── queries.sql
├── docs/
│   ├── report.tex
│   ├── report.docx
│   ├── figures/
│   └── references.bib
├── tests/
└── config.example
```

Do not place files in directories that are not actually used. Preserve the original template's necessary files and directory relationships.

## 15. Required Workflow

Complete the work in this order:

**Phase 1 — Inspect the source materials**

- List the ZIP's contents.
- Identify the main LaTeX entry point and all required supporting files.
- Examine the sample PDF's structure and content.
- Identify required report sections and formatting constraints.
- Report any unreadable files or unresolved ambiguities.

**Phase 2 — Plan the project**

- Confirm the permitted technology stack.
- Define functional and non-functional requirements.
- Design the database schema and relationships.
- Define the transaction workflows.
- Decide which optional features are feasible.
- Map the report's required sections to the actual project deliverables.

**Phase 3 — Implement the database**

- Write and run the schema.
- Add synthetic sample data.
- Validate keys, constraints, and relationships.
- Test essential SQL queries.
- Document normalization and integrity decisions.

**Phase 4 — Implement the application**

- Build the application layers.
- Connect to the database.
- Implement authentication and core transactions.
- Implement the UI.
- Add validation and error handling.
- Verify that the application operates against the database rather than using fake in-memory transaction results.

**Phase 5 — Test**

- Run the application.
- Execute the supported test cases.
- Fix actual defects.
- Record real results and remaining limitations.

**Phase 6 — Produce the reports**

- Populate the official LaTeX template.
- Generate the editable Word report.
- Add accurate diagrams, tables, SQL examples, and verified screenshots where required.
- Compare the two report formats for content consistency.
- Compile the LaTeX report if the required tools are available.

**Phase 7 — Final verification**

- Check the completeness of the source code.
- Verify database setup instructions.
- Check the report against the template.
- Confirm the `.docx` opens and is editable.
- Ensure no fabricated information or unexecuted test claims are presented as facts.
- Update `README.md` with final instructions and verified status.

## 16. Final Acceptance Criteria

The project is complete only when:

1. The application source code is present.
2. The database schema and sample-data scripts are present.
3. The core ATM operations are implemented and connected to the database.
4. Financial updates and their transaction records are handled consistently.
5. The application has been tested to the extent possible in the available environment.
6. The official LaTeX template has been followed.
7. The LaTeX report and editable Word report are both supplied.
8. The report reflects the actual implementation rather than imaginary features.
9. The README contains reproducible setup and run instructions.
10. Known limitations and unexecuted tests are explicitly documented.

## 17. Final Instruction

Do not begin by generating a generic report or a collection of disconnected code snippets.

First inspect the supplied files and establish the template requirements. Then plan, implement, test, document, and verify the complete ATM Transaction System.

Prioritize **database correctness, reliable transaction handling, template compliance, report completeness, and reproducibility** over unnecessary complexity or visual decoration.

At the end, provide a concise inventory of all deliverables, the s
teps to run the project, the database setup instructions, the report compilation status, the Word document status, and any remaining student-supplied information.


This is how I want the Frontend looking like, take insperations from - https://sight-banking.webflow.io/ and https://www.contiant.com/

---

# PROJECT JOURNAL & IMPLEMENTATION DELIVERABLES

## 18. Project Journal

### 18.1 Chronological Development Journal
* **Phase 1 — Inspection of Reference Materials**:
  * Inspected `Course End Report Latex Template.zip` (`template_extracted/Course End Project Report/main.tex`). Extracted and analyzed required document class (`report`), margins (1 inch), institutional structure for Vardhaman College of Engineering, Dept of CSE, title page, abstract, table of contents, methodology, results, POs/SDGs mapping, and IEEE bibliography conventions.
  * Inspected sample report `Fitness_Center_DBMS_Report_edited.pdf` (75 pages). Identified academic depth requirements: ER modeling, relational schema, formal normalization proofs (1NF through BCNF), detailed SQL query implementations (joins, aggregates, subqueries), transaction processing (COMMIT/ROLLBACK/SAVEPOINT), concurrency anomaly demonstrations (lost update, double booking), physical file storage and B+ Tree indexing analysis, and a structured test case suite.
* **Phase 2 — Technology Selection & Design Decisions**:
  * **Application Core**: Java 21 LTS with pure object-oriented design and standard library idioms.
  * **Database Engine**: MySQL 8.x with InnoDB transactional storage engine, enforced foreign keys with cascade updates, and CHECK constraints preventing negative balances (`CHECK (balance >= 0)`).
  * **Connectivity**: JDBC with a thread-safe connection pool (`DatabaseConnection`) implemented via Java's `ArrayBlockingQueue`. All queries strictly use parameterized `PreparedStatement` to eliminate SQL injection vulnerabilities.
  * **Security**: PIN and password authentication enforced using one-way salted BCrypt hashing (`at.favre.lib:bcrypt:0.10.2`) with cost factor 12 (4,096 rounds), completely avoiding plaintext credential storage. Automated brute-force lockout triggered after 3 consecutive failed attempts.
  * **Frontend Design**: Java Swing enhanced with FlatLaf Dark Look-and-Feel, styled after Sight Banking (dark fintech) and Contiant (enterprise security). Employs deep navy (`#0B1121`), slate (`#1E293B`), emerald green (`#10B981`), and cyan (`#06B6D4`) accent palettes with custom-painted rounded cards and hover states.
* **Phase 3 — Database Implementation & Verification**:
  * Created `database/schema.sql` defining 7 normalized tables: `customers`, `accounts`, `atm_cards`, `transactions`, `transfers`, `admins`, and `login_attempts`.
  * Verified normalization: eliminated all partial and transitive dependencies; proved BCNF compliance since all functional dependency determinants are superkeys.
  * Created `database/sample_data.sql` with 5 realistic customers, 7 bank accounts (Savings and Current), 7 ATM cards, 1 administrator, sample transaction histories, and inter-account transfers.
  * Identified and resolved bug in initial seed data: replaced mock hash placeholders with genuine, verified BCrypt hashes for PINs `1234`, `5678`, `9012`, `3456`, `7890`, and admin password `admin123`.
  * Created `database/queries.sql` implementing 10 analytical, audit, and reporting queries covering joins, aggregates, subqueries, and shortfall analysis.
* **Phase 4 — Software Architecture & Application Development**:
  * Designed and built 3-Tier Layered Architecture:
    * `com.atm.model`: Domain entities (`Customer`, `Account`, `ATMCard`, `Transaction`, `Transfer`, `Admin`).
    * `com.atm.dao`: Data access objects (`AccountDAO`, `ATMCardDAO`, `CustomerDAO`, `TransactionDAO`, `TransferDAO`, `AdminDAO`, `LoginAttemptDAO`).
    * `com.atm.service`: Transaction orchestration and validation (`AuthenticationService`, `TransactionService`, `AdminService`).
    * `com.atm.ui`: Swing user interface with `MainFrame` (CardLayout), `LoginPanel`, `MainMenuPanel`, `BalancePanel`, `WithdrawPanel`, `DepositPanel`, `TransferPanel`, `MiniStatementPanel`, `AdminLoginPanel`, and `AdminPanel`.
    * `com.atm.util`: `ConfigManager`, `DatabaseConnection`, `PasswordHasher`.
    * `com.atm.ATMApplication`: Entry point with FlatLaf initialization, connection pool startup, and shutdown hooks.
* **Phase 5 — Automated Testing Suite & Self-Service Registration**:
  * Implemented `src/test/java/com/atm/test/` containing `PasswordHasherTest`, `ConfigManagerTest`, `ModelTest`, `ValidationTest`, `RegistrationServiceTest`, and `ATMSuiteTestRunner`.
  * Added self-service sign-up workflow: `RegistrationService` and `SignUpPanel` allowing user-defined custom card numbers (e.g. `4532-1234-5678-9010`) or auto-generated 16-digit cards, opening balance deposit, and BCrypt PIN establishment.
  * Formulated test cases covering all 15 scenarios from Section 10 of Readme.md plus registration and validation test suites.
  * Executed the automated test suite on Windows 11 with OpenJDK 21: **45 test cases executed, 45 passed, 0 failed (100% pass rate)**.
* **Phase 6 — Project Reports (LaTeX and Word .docx)**:
  * Created `docs/report.tex` adhering strictly to the official template structure, including title page, abstract, introduction, literature review, problem analysis, system design, normalization proofs, implementation, SQL queries, transaction processing, indexing, testing results, POs/SDGs mapping, and IEEE bibliography.
  * Generated high-resolution diagrams (`docs/er_diagram.png` and `docs/architecture_diagram.png`).
  * Created `docs/report.docx` via `python-docx` providing a genuinely editable, formatted Microsoft Word document with full content parity, styled headings, data tables, callouts, and embedded figures.
* **Phase 7 — Verification & Scripts**:
  * Created `build.bat`, `run.bat`, and `test.bat` scripts for one-click compilation, execution, and testing on Windows.

---

## 19. Deliverables Inventory

| Deliverable | Path | Description |
| :--- | :--- | :--- |
| **Main Application** | `src/main/java/com/atm/ATMApplication.java` | Main desktop entry point with FlatLaf dark theme & pool initialization |
| **UI Components (12 files)** | `src/main/java/com/atm/ui/` | Sight Banking & Contiant styled Swing panels with CardLayout navigation (including `SignUpPanel`) |
| **Service Layer (4 files)** | `src/main/java/com/atm/service/` | Business rules, BCrypt authentication, atomic ACID transaction workflows, and `RegistrationService` |
| **DAO Layer (7 files)** | `src/main/java/com/atm/dao/` | SQL execution via parameterized `PreparedStatement` |
| **Model Entities (6 files)** | `src/main/java/com/atm/model/` | Domain POJOs representing accounts, cards, customers, transactions |
| **Utilities (3 files)** | `src/main/java/com/atm/util/` | `ConfigManager`, `DatabaseConnection` pool, `PasswordHasher` |
| **Database DDL** | `database/schema.sql` | 7 normalized tables, constraints, foreign keys, InnoDB engine |
| **Database DML** | `database/sample_data.sql` | Synthetic dataset with verified BCrypt hashes for PINs & admin |
| **SQL Queries** | `database/queries.sql` | 10 analytical, audit, and reporting SQL queries |
| **Automated Test Suite** | `src/test/java/com/atm/test/` | 45 test assertions covering all Section 10 scenarios + registration (100% pass rate) |
| **LaTeX Report** | `docs/report.tex` | Complete academic report adhering to the official template |
| **Word Report (.docx)** | `docs/report.docx` | Fully editable, formatted Microsoft Word report (1.04 MB) |
| **ER Diagram** | `docs/er_diagram.png` | High-resolution relational schema and entity relationship diagram |
| **Architecture Diagram** | `docs/architecture_diagram.png` | 3-Tier Layered Architecture diagram |
| **Windows Build Script** | `build.bat` | One-click Java compiler script |
| **Windows Launch Script** | `run.bat` | One-click application launcher |
| **Windows Test Script** | `test.bat` | One-click automated test runner |

---

## 20. Database Setup & Execution Instructions

### 20.1 Database Setup (MySQL 8.x)
Ensure MySQL Server is running locally. Execute the following commands in MySQL Workbench, MySQL Shell, or command line:

```bash
# 1. Log in to MySQL
mysql -u root -p

# 2. Import Schema (creates database 'atm_transaction_system' and 7 tables)
source database/schema.sql;

# 3. Import Sample Dataset (inserts customers, accounts, cards, verified hashes)
source database/sample_data.sql;
```

Verify database credentials in `config.properties` (or copy from `config.example.properties`):
```properties
db.url=jdbc:mysql://localhost:3306/atm_transaction_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=your_mysql_password
```

### 20.2 Building the Project
Double-click `build.bat` or run in terminal:
```cmd
.\build.bat
```
This compiles all 37 Java source files (main application + test suite) into the `bin/` directory using the bundled JARs in `lib/`.

### 20.3 Running the Automated Test Suite
Double-click `test.bat` or run in terminal:
```cmd
.\test.bat
```
Output:
```text
============================================================
                   TEST EXECUTION SUMMARY
============================================================
  Total Tests Run : 45
  Passed          : 45
  Failed          : 0
  Pass Rate       : 100.0%
============================================================
  STATUS: ALL TESTS PASSED SUCCESSFULLY! (PASS)
============================================================
```

### 20.4 Launching the Application
Double-click `run.bat` or run in terminal:
```cmd
.\run.bat
```

### 20.5 Default Demonstration Credentials
* **Customer ATM Cards**:
  * Card Number: `4532-1234-5678-9010` (Aarav Sharma — Savings, Balance: ₹75,000) | **PIN: `1234`**
  * Card Number: `4532-3456-7890-1232` (Priya Reddy — Savings, Balance: ₹32,500) | **PIN: `1234`**
  * Card Number: `4532-4567-8901-2343` (Vikram Patel — Savings, Balance: ₹120,000) | **PIN: `5678`**
  * Card Number: `4532-7890-1234-5676` (Rohan Kumar — Card Status: **BLOCKED**) | PIN: `7890`
* **Bank Administrator**:
  * Username: `admin` | **Password: `admin123`**