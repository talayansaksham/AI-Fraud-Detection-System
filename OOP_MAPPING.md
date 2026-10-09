# Rubric Concept Mapping

Use this table directly in your viva/demo — it points to the exact class and line-level feature for every rubric item.

## OOP Implementation

| Concept | Class / Location | How it's used |
|---|---|---|
| Encapsulation | `User`, `Transaction`, `Alert` | All fields `private`, accessed only via getters/setters |
| Constructors (overloaded) | `Transaction` | One constructor for a *new* transaction (no ID yet), another for loading an *existing* row from the DB |
| Interface | `FraudDetector` | Declares `check()` and `getName()` — the contract every detection rule follows |
| Polymorphism | `FraudDetectionService.evaluate()` | Loops over `List<FraudDetector>` and calls `detector.check(txn)` — the actual method that runs depends on the real object (`AmountRuleDetector`, `VelocityRuleDetector`, etc.) at runtime |
| Exception Handling (custom) | `InvalidTransactionException`, used in `AddTransactionPanel.buildAndValidateTransaction()` | Thrown for bad user input (negative amount, blank location) before it ever reaches the database |
| Exception Handling (built-in) | Every DAO method | `SQLException` caught/thrown around all JDBC calls |
| Access Modifiers | `DBConfig` (private constructor, public static fields), all model classes (private fields, public methods) | Demonstrates deliberate use of `private`/`public` |

## Collections & Generics

| Concept | Class / Location | Why it's used |
|---|---|---|
| `List<FraudDetector>` | `FraudDetectionService` | Holds any number/mix of detection rules without knowing their concrete types — real generics + polymorphism together |
| `List<Transaction>`, `List<User>`, `List<Alert>` | DAO `getAll...()` methods | Natural fit for "rows returned from a query" |
| `List<String>` (generic) | `DetectionResult.reasons` | Collects however many rule violations a transaction triggers |
| `List<MiniBarChart.Bar>` | `DashboardPanel.refresh()` | Feeds the custom chart component a generic list of data points |

*(HashMap was considered for a username→User lookup cache but skipped — the app's scale doesn't need it, and adding it would be a collection used "for marks" rather than for a reason, which the assignment explicitly says to avoid.)*

## Multithreading & Synchronization

| Concept | Class / Location | Why it's needed (not forced) |
|---|---|---|
| `Runnable` | `TransactionSimulator implements Runnable` | Generates a new sample transaction every few seconds to simulate a live feed |
| `Thread` | `MainDashboard.toggleSimulation()` — `new Thread(simulator, "TransactionSimulatorThread")` | Runs the simulator off the Swing Event Dispatch Thread so clicking buttons/scrolling never freezes while transactions are being generated |
| `synchronized` methods | `TransactionSimulator.startSimulation()`, `stopSimulation()`, `isRunning()` | The GUI thread (Start/Stop button) and the simulator thread both read/write the `running` flag — synchronization prevents a race condition |
| Thread-safe GUI updates | `MainDashboard` — `SwingUtilities.invokeLater(this::refreshAllScreens)` | Swing components must only be touched from the Event Dispatch Thread, so updates triggered from the background thread are marshalled back correctly |

## Database Connectivity (JDBC)

| Concept | Class / Location |
|---|---|
| Driver/connection setup | `DBConnection.getConnection()` |
| Centralized credentials | `DBConfig` (one file, not hardcoded in every DAO) |
| PreparedStatement | Every DAO method (`UserDAO`, `TransactionDAO`, `AlertDAO`) — prevents SQL injection |
| ResultSet | `getAllUsers()`, `getAllTransactions()`, `getAllAlerts()`, `getUserById()`, `authenticate()` |
| Generated keys | `addUser()`, `addTransaction()`, `addAlert()` use `Statement.RETURN_GENERATED_KEYS` |
| try-with-resources | All DAO methods — Connection/PreparedStatement/ResultSet auto-closed |
| DAO classes for DB operations | `UserDAO`, `TransactionDAO`, `AlertDAO` — SQL lives only here, never in the GUI |
| SQL JOIN | `AlertDAO.getAlertsByUser()` — joins `alerts` and `transactions` since alerts aren't directly linked to a user |
| Per-user filtering (WHERE clause) | `TransactionDAO.getTransactionsByUser()`, `AlertDAO.getAlertsByUser()` — powers the customer app's data isolation |

## Database Design

| Concept | Where |
|---|---|
| Primary keys | `user_id`, `txn_id`, `alert_id` (all `AUTO_INCREMENT`) |
| Foreign keys | `transactions.user_id → users.user_id`, `alerts.txn_id → transactions.txn_id` |
| Relationships | Users (1) → Transactions (N) → Alerts (N) |
| Real query justification | `TransactionDAO.countRecentTransactionsByUser()` directly powers `VelocityRuleDetector` — not a decorative query |

## Bonus: ML Classifier (optional, not required for core functionality)

| Concept | Location |
|---|---|
| Real trained model | `MLClassifierDetector` — trains a Weka `J48` decision tree from `resources/training_data.arff` at construction time |
| Same interface as rule-based detectors | Implements `FraudDetector`, so `FraudDetectionService` treats it identically to the rule-based checks (polymorphism again) |
| Fail-safe design | If training fails (e.g., `weka.jar` missing), `ready = false` and `check()` simply returns `null` instead of crashing the app |

## Bonus: Two-Sided Design (Bank + Customer)

| Concept | Location |
|---|---|
| Shared wiring, no duplication | `AppContext` — builds DAOs and detectors exactly once; both `Main` (bank) and `CustomerMain` (customer) take the same context, so the two apps can never drift apart on rules/thresholds |
| Two independent entry points | `Main.java` (bank/analyst, sees everything) vs `CustomerMain.java` (customer, sees only their own data) — same underlying service/DAO layer, different GUI and different data scope |
| Authentication | `LoginPanel` + `UserDAO.authenticate()` — customer side requires login; bank side is treated as an internal tool (documented as a scoping decision, not an oversight) |
| Data isolation per user | `TransactionDAO.getTransactionsByUser()`, `AlertDAO.getAlertsByUser()` — every customer-side query is scoped to `currentUser.getUserId()` |
| Same architecture, different callers | `MakePaymentPanel` (customer) and `AddTransactionPanel` (bank) both call the identical `transactionDAO.addTransaction()` + `detectionService.evaluate()` — proving the two apps are two doors into the same system, not two different systems |
