<<<<<<< HEAD
# Fraud Detection System

A Java Swing desktop application that detects suspicious financial transactions in real time. It ships as **two apps on one shared MySQL database**: a **Bank / Analyst app** (monitoring, administration) and a **Customer app** (sign-up, payments, and a "Was this you?" review of flagged payments).

## Why this project

Manual review of transactions is slow and misses patterns that are obvious when checked systematically: an unusually large amount, too many payments in a short window, or a payment from a place the customer never uses. This project automates those checks and puts the customer in the loop, the way real banks do.

## Features

**Customer app** (`CustomerMain`)
- Sign up and log in; each customer only ever sees their own data
- Make a payment, which runs through the fraud rules instantly with a live risk-confidence meter
- "Was This You?" screen: confirm a flagged payment (it is approved) or deny it (it is blocked)

**Bank / Analyst app** (`Main`)
- Dashboard: live totals, recent-amount chart, recent transactions with status badges
- Add transactions manually, manage users, and view the full alert audit log
- **Resolution column** on every alert: `PENDING` (customer has not responded), `APPROVED` (customer confirmed it was them) or `BLOCKED` (customer denied it)
- **System Config**: change the amount threshold, velocity limit and time window without touching code
- **Algorithms**: switch individual detectors on/off (Amount, Velocity, Location, ML classifier) and reload the ML model
- **Live simulation**: a background thread generates transactions so the dashboard updates in real time

**Detection** (all implement one `FraudDetector` interface)
- Amount rule, Velocity rule, Location rule
- Optional Weka **J48 decision tree** trained from `resources/training_data.arff`

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java (JDK 11+) |
| GUI | Swing, custom-painted components |
| Database | MySQL 8 via JDBC (DAO pattern, PreparedStatement) |
| ML (optional) | Weka J48 |
| Build | Maven (dependencies download automatically) |
| Concurrency | `Thread` / `Runnable`, `synchronized` |

## Architecture

`GUI -> Service -> DAO -> JDBC -> MySQL`. The GUI never contains SQL. Both apps build one shared `AppContext` (DAOs plus the detection service), so they always apply identical rules.

```
src/com/frauddetection/
├── model/       User, Transaction, Alert
├── dao/         DBConnection, UserDAO, TransactionDAO, AlertDAO
├── service/     FraudDetector + detectors, FraudDetectionService, TransactionSimulator
├── exception/   InvalidTransactionException
├── util/        DBConfig (reads db.properties)
├── gui/         screens for both apps, plus shared theme components
├── AppContext.java
├── Main.java            (bank app entry point)
└── CustomerMain.java    (customer app entry point)
resources/       schema.sql, reset_demo_data.sql, training_data.arff
```

## Setup

**Requirements:** JDK 11+, MySQL 8, Maven (or an IDE with Maven support, e.g. IntelliJ / VS Code / Antigravity).

1. **Create the database**
   ```
   mysql -u root -p < resources/schema.sql
   ```
2. **Add your DB credentials.** Copy `db.properties.example` to `db.properties` (same folder as `pom.xml`) and set your MySQL password. This file is git-ignored, so your password is never uploaded.
3. **Run**
   - In your IDE, run `com.frauddetection.Main` (bank app) and `com.frauddetection.CustomerMain` (customer app), or
   - From a terminal in the project folder:
     ```
     mvn compile org.codehaus.mojo:exec-maven-plugin:3.5.1:java -Dexec.mainClass=com.frauddetection.Main
     mvn compile org.codehaus.mojo:exec-maven-plugin:3.5.1:java -Dexec.mainClass=com.frauddetection.CustomerMain
     ```

Open both apps at the same time to see them share data.

## Demo accounts

| Username | Password |
|---|---|
| rahul_k | pass123 |
| priya_s | pass123 |
| aman_v | pass123 |

Or create a new account from the customer app's sign-up screen. It appears on the bank app's Users screen straight away.

## Try the full flow

1. Customer app: log in as `rahul_k` and pay an amount above the threshold, or use location `Unknown-VPN`. The payment is flagged and the risk meter jumps.
2. Bank app, **Alerts**: the new alert shows `PENDING`.
3. Customer app, **Was This You?**: click "No, this wasn't me".
4. Bank app, **Alerts**: the same alert now shows `BLOCKED`. If the customer clicks "Yes, this was me" instead, it shows `APPROVED`.

## Screenshots

| Bank app | |
|---|---|
| Dashboard | ![Dashboard](screenshots/bank-dashboard.png) |
| Alerts with resolution | ![Alerts](screenshots/bank-alerts.png) |
| Users | ![Users](screenshots/bank-users.png) |
| Add transaction | ![Add Transaction](screenshots/bank-add-transaction.png) |
| System config | ![System Config](screenshots/bank-system-config.png) |
| Algorithms | ![Algorithms](screenshots/bank-algorithms.png) |

| Customer app | |
|---|---|
| Login | ![Login](screenshots/customer-login.png) |
| Home | ![Home](screenshots/customer-home.png) |
| Make a payment | ![Payment](screenshots/customer-payment.png) |
| Was this you? | ![Was This You](screenshots/customer-was-this-you.png) |

## Concepts demonstrated

See [`OOP_MAPPING.md`](./OOP_MAPPING.md) for exactly where each concept lives: OOP (interfaces, polymorphism, inheritance, exceptions), collections and generics, multithreading and synchronization, JDBC and DAO classes, SQL joins.

## Limitations (honest scope)

This is a prototype that demonstrates the architecture of a two-sided fraud system. It is not production-ready:
- Passwords are stored as plain text. A real system must use a salted hash (e.g. BCrypt).
- No real-time latency guarantee, no encryption in transit, no compliance layer (PCI-DSS, audit trails).
- Single MySQL instance and single JVM per app; real volumes need queues and horizontal scaling.
- The ML model trains on a small sample file, not real labelled data.

## Team

- Name: CodeAlpha____________________
=======
# AI-Fraud-Detection-System
>>>>>>> 7b76e2bf7c12d88f5bf7332c25b96e1d387b82c5
