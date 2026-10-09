# Fraud Detection System

A Java Swing desktop application that detects potentially fraudulent financial transactions using rule-based checks (and an optional trained ML classifier), with a real-time transaction simulation and a dashboard UI.

## Description

Manual review of financial transactions is slow and misses patterns that are only obvious when checked systematically — for example, an unusually large amount, too many transactions from the same user in a short window, or a transaction from a location the user doesn't normally use. This project automates that check: every transaction is run through a set of detection rules, flagged if suspicious, and shown on a live dashboard.

## Features

**Bank / Admin side (`Main.java`):**
- **Admin Authentication gate** (default password: `admin123`)
- Register users and record transactions on their behalf
- **System Configuration Panel** — dynamically configure detection thresholds (amount threshold, velocity count, time window) at runtime
- **Algorithm Management Panel** — enable/disable specific detection rules and reload/re-train the ML model at runtime
- Automatic fraud detection on every transaction using pluggable detectors:
  - **Amount Rule** — flags unusually large transactions
  - **Velocity Rule** — flags too many transactions from one user in a short time window
  - **Location Rule** — flags a transaction from a location that doesn't match the user's usual one
  - **ML Classifier (Weka J48 Decision Tree)** — machine learning classifier trained on transaction data
- Alerts are stored and viewable in a dedicated screen
- **Live simulation** — a background thread generates sample transactions automatically so the dashboard updates in real time
- Dashboard with summary stats, risk meters, and a trend chart of recent transaction amounts

**Customer side (`CustomerMain.java` or launcher in `Main.java`):**
- Login screen — a customer only ever sees their own data, never anyone else's
- **Make a Payment** — submits a transaction through the fraud detection pipeline
- **Instant Security Alert** — immediate warning notification if a transaction is flagged as high risk
- **"Was This You?"** — customer alerts screen where customers review flagged transactions and confirm or dispute them directly
- Both apps read/write the same MySQL database via a shared `AppContext`

**Honest scope note:** running two Swing apps against one local MySQL database demonstrates the *architecture* of a two-sided fraud system (separate audiences, shared detection logic, per-user data isolation). It is not production-ready — see [Limitations](#limitations--real-world-gap) below.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Core Java (JDK 11+) |
| Database | MySQL 8.x via JDBC |
| GUI | Java Swing (Nimbus look-and-feel + custom styling) |
| Concurrency | Java Thread/Runnable, `synchronized` |
| Bonus ML | Weka (J48 decision tree) |

## Project Structure

```
FraudDetectionSystem/
├── src/com/frauddetection/
│   ├── model/          # User, Transaction, Alert (POJOs)
│   ├── dao/             # DBConnection, UserDAO, TransactionDAO, AlertDAO (all JDBC/SQL lives here)
│   ├── service/         # FraudDetector interface + rule implementations, FraudDetectionService,
│   │                     TransactionSimulator, DetectionResult, MLClassifierDetector (bonus)
│   ├── exception/       # InvalidTransactionException
│   ├── util/            # DBConfig (single place for DB credentials)
│   ├── gui/              # MainDashboard, DashboardPanel, AddTransactionPanel, UsersPanel, AlertsPanel
│   │                      (bank side); LoginPanel, CustomerApp, CustomerHomePanel, MakePaymentPanel,
│   │                      MyAlertsPanel (customer side); UITheme, RoundedPanel, StyledButton, MiniBarChart (shared)
│   ├── AppContext.java  # builds DAOs + detection service ONCE, shared by both apps
│   ├── Main.java        # entry point — BANK / ANALYST app
│   └── CustomerMain.java # entry point — CUSTOMER app
├── resources/
│   ├── schema.sql        # MySQL table definitions + sample data (fresh install)
│   ├── migration_add_password.sql  # run only if you created the DB before password_hash existed
│   └── training_data.arff # sample labeled data for the bonus ML classifier
├── lib/                  # place mysql-connector-j.jar (required) and weka.jar (optional) here
├── OOP_MAPPING.md        # rubric concept → exact class/line mapping
├── README.md
└── .gitignore
```

**Architecture:** `GUI → Service → DAO → JDBC → MySQL` — the GUI never writes SQL directly; it always goes through the service layer, which goes through the DAO layer. Both `Main` and `CustomerMain` build one `AppContext` each and share the identical DAO/detector wiring, so the two apps can never silently drift apart (e.g. different fraud thresholds).

## Requirements

- JDK 11 or higher
- MySQL 8.x
- [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) (JDBC driver — required)
- [Weka](https://waikato.github.io/weka-wiki/downloading_weka/) (only if you enable the bonus ML classifier)

## Database Setup

1. Start MySQL and run the schema script:
   ```bash
   mysql -u root -p < resources/schema.sql
   ```
   This creates the `fraud_detection` database, all three tables, and a few sample rows — including 3 demo customer logins (see below).
   **Already have the database from before the customer-login feature?** Run `resources/migration_add_password.sql` instead of recreating everything from scratch — it just adds the missing column.
2. Open `src/com/frauddetection/util/DBConfig.java` and update `USERNAME`/`PASSWORD` to match your MySQL setup.

## How to Run

1. Download `mysql-connector-j-<version>.jar` and place it in `lib/`.
2. Compile:
   ```bash
   javac -cp "lib/*" -d bin $(find src -name "*.java")
   ```
   *(To also compile the bonus ML classifier, add `weka.jar` to `lib/` first — it's excluded above by default since it's optional.)*
3. Run the **bank/analyst app**:
   ```bash
   java -cp "bin:lib/*" com.frauddetection.Main
   ```
4. Run the **customer app** (in a separate terminal — it's a separate window/process, sharing the same database):
   ```bash
   java -cp "bin:lib/*" com.frauddetection.CustomerMain
   ```
   Log in with one of the sample accounts: `rahul_k` / `priya_s` / `aman_v`, password `pass123`.

   *(On Windows, use `;` instead of `:` in the classpath: `java -cp "bin;lib/*" com.frauddetection.Main`)*

**Try this to see both sides connect:** make a payment on the customer app with a large amount (>₹5000) or location "Unknown-VPN" → it gets flagged → switch to the bank app's Alerts screen (or refresh the Dashboard) → the same alert appears there, since both apps share one MySQL database via `AppContext`.

## Machine Learning Classifier (Weka J48)

The system includes a trained **Weka J48 Decision Tree** model (`MLClassifierDetector.java`) that operates alongside the rule-based detectors. `weka-stable-3.8.6.jar` is included in `lib/`.
- Training dataset: `training_data.arff` (labeled transaction amounts, hours, VPN flags, and fraud class).
- Dynamic retraining/reloading: Admin can retrain or reload the model on demand from the **Algorithms** tab.

## Screenshots

*(Add screenshots of the Dashboard, Add Transaction, Users, Alerts, System Config, and Algorithm screens here after running the app.)*

## Rubric Coverage

See [`OOP_MAPPING.md`](./OOP_MAPPING.md) for exactly where every rubric concept (OOP, Collections, Generics, Multithreading, JDBC, DAO classes) is implemented.

## Limitations & Real-World Gap

This project demonstrates the *shape* of a two-sided fraud detection system (bank + customer, shared detection logic, per-user data isolation) — it is intentionally not production-grade. Specifically:

- **Passwords are stored in plain text** for simplicity. A real system must use a salted hash (e.g. BCrypt) and never select the password back out of the database at all.
- **No real-time constraint.** A real payment must be scored in well under 100ms, inline with the transaction. This project has no such latency requirement.
- **Single MySQL instance, single JVM per app.** Real transaction volume needs horizontal scaling, message queues (Kafka), and no single point of failure.
- **The ML classifier trains on 40 sample rows.** Real fraud models train on millions of labeled transactions and are retrained continuously as patterns shift.
- **No encryption in transit, no compliance layer** (PCI-DSS, RBI guidelines, audit trails) — required for any real financial product.

Naming these gaps explicitly (rather than pretending the prototype is production-ready) is itself part of demonstrating understanding of the problem space — worth raising directly in viva if asked "would this work in a real bank?"

## Status / Roadmap

- ✅ Review 1: project structure, database design, DB connectivity, UI/UX design
- ✅ Core implementation: DAO/JDBC layer, rule-based detection, multithreaded simulation, Swing dashboard
- ✅ Machine Learning: trained Weka J48 classifier active with dynamic reloading & toggle controls
- ✅ System Configuration & Algorithm Management panels for Admin
- ✅ Immediate Security Alert popups & dispute review for Customers
- ⏳ Unit tests (JUnit) — planned for next milestone

## Team

- Name / Roll No.: ____________________
