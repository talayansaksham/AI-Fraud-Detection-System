# Viva Preparation — Fraud Detection System

Simple, beginner-friendly Hinglish answers. Read these in your own words during viva, don't cram word-for-word.

---

**1. Why did you choose this project?**
Fraud detection ek real-world problem hai — banks aur payment apps ko har second hazaron transactions check karne padte hain. Maine socha ki isme Java ke concepts (OOP, collections, threading, JDBC) sab naturally fit ho jaate hain, sirf dikhawe ke liye force nahi karna padta.

**2. What problem does it solve?**
Manual transaction checking slow hai aur mistakes hoti hain. Yeh system automatically har transaction ko rules ke against check karta hai aur suspicious wale ko turant flag kar deta hai.

**3. Why did you use Java?**
Java strongly-typed hai, OOP achhe se support karta hai, aur JDBC ke through database se connect karna straightforward hai. GUI ke liye Swing bhi built-in hai, extra setup nahi chahiye.

**4. What is inheritance in your project?**
Is version mein explicit inheritance (extends) nahi hai — maine interface (`FraudDetector`) use kiya hai polymorphism dikhane ke liye, kyunki alag-alag detection rules ke beech "IS-A" relationship nahi, balki "CAN-DO" relationship hai (sab check() kar sakte hain, alag tareeke se).

**5. Where is polymorphism used?**
`FraudDetectionService.evaluate()` mein — yeh `List<FraudDetector>` par loop karta hai aur `detector.check(txn)` call karta hai. Runtime par yeh decide hota hai ki actual mein `AmountRuleDetector` ka check() chalega ya `VelocityRuleDetector` ka — yehi runtime polymorphism hai.

**6. Where is the interface?**
`FraudDetector` interface hai jisme `check()` aur `getName()` methods declare hain. `AmountRuleDetector`, `VelocityRuleDetector`, `LocationRuleDetector` (aur bonus `MLClassifierDetector`) sab isko implement karte hain.

**7. Why did you use ArrayList / List?**
`List<FraudDetector>` mein maine jitne bhi rules add karne hain utne add kar sakta hoon, bina code change kiye. `List<Transaction>`, `List<User>` — DAO se multiple rows aane par natural fit hai.

**8. Why did you use HashMap?**
Maine is project mein HashMap force nahi kiya — jahan zaroorat nahi thi wahan add nahi kiya, kyunki sirf marks ke liye collection dalna galat practice hai.

**9. Where is generics used?**
Har jagah `List<T>` — jaise `List<FraudDetector>`, `List<Transaction>`, `List<String>` (DetectionResult ke reasons ke liye). Generics se type-safety milti hai — galat type ka object list mein daal hi nahi sakte, compile-time par hi error aa jayega.

**10. Why is multithreading required?**
Live transaction simulation ke liye — agar main sabkuch main thread (GUI thread) par karta, to jab tak transactions generate ho rahi hain UI freeze ho jaata, buttons click nahi hote. Isliye simulation ek alag background thread par chalta hai.

**11. What is synchronization?**
Jab do threads (GUI thread aur simulator thread) ek hi shared variable (`running` flag) ko read/write karte hain, to race condition ho sakti hai — matlab ek thread purana value dekh sakta hai. `synchronized` keyword lagane se ek time par sirf ek thread us method ko execute kar sakta hai, isse data consistent rehta hai.

**12. What is JDBC?**
JDBC (Java Database Connectivity) ek API hai jo Java program ko MySQL jaisi database se connect karne deta hai. Isse hum Java code se SQL queries bhej sakte hain aur result wapas Java objects mein le sakte hain.

**13. How does JDBC connect Java to MySQL?**
`DriverManager.getConnection(url, username, password)` call karte hi ek `Connection` object milta hai jo MySQL server se socket connection banata hai. Uske baad us Connection se `PreparedStatement` bana kar hum query bhejte hain.

**14. What is PreparedStatement?**
Yeh ek precompiled SQL query hai jisme `?` placeholders hote hain, jo baad mein actual values se replace hote hain (`ps.setString(1, value)`). Isse SQL injection attack nahi ho sakta, kyunki value ko query ke syntax ka part nahi banaya jaata.

**15. What is ResultSet?**
Jab hum `SELECT` query chalate hain, to database se jo rows wapas aati hain woh `ResultSet` object mein hoti hain. Hum `rs.next()` se row-by-row loop karte hain aur `rs.getString()`, `rs.getInt()` se columns nikalte hain.

**16. What is DAO?**
DAO (Data Access Object) ek design pattern hai jisme har database table ke liye ek alag class banate hain (jaise `UserDAO`, `TransactionDAO`) jo sirf us table ka SQL handle karti hai. GUI ya service layer ko SQL likhna hi nahi padta.

**17. Why separate database classes?**
Separation of concerns ke liye — agar kal database MySQL se PostgreSQL change karna ho, to sirf DAO classes change karni padengi, GUI code touch nahi karna padega. Code bhi zyada readable aur maintainable hota hai.

**18. What happens if the database connection fails?**
`SQLException` throw hoti hai, jo hum catch karte hain aur user ko ek friendly error dialog dikhate hain (`JOptionPane`), instead of app crash hone ke.

**19. What is exception handling?**
Yeh ek mechanism hai jisse hum runtime errors ko gracefully handle kar sakte hain, bina program crash kiye. Maine ek custom exception `InvalidTransactionException` banaya hai jo tab throw hota hai jab user galat input de (jaise negative amount).

**20. Explain your database tables.**
Teen tables hain: `users` (user ki details aur usual location), `transactions` (har transaction ka amount, location, category, status), aur `alerts` (jab koi transaction flag hoti hai to uska risk level aur reason). Users ke multiple transactions ho sakte hain, aur ek flagged transaction ka ek alert record banta hai.

**21. What are primary and foreign keys?**
Primary key ek column hai jo har row ko uniquely identify karta hai (jaise `user_id`). Foreign key ek table ka column hai jo dusri table ke primary key ko reference karta hai — jaise `transactions.user_id` jo `users.user_id` ko point karta hai. Isse relationships maintain hote hain.

**22. Explain your project architecture.**
Layered architecture hai: GUI (Swing screens) → Service (fraud detection logic) → DAO (JDBC/SQL) → MySQL. Har layer sirf apne neeche wali layer se baat karta hai, GUI seedha database se baat nahi karta.

**23. What is the Velocity Rule and why does it matter?**
Yeh check karta hai ki ek user ne kitne transactions kiye hain last few minutes mein. Agar bahut zyada hain, to suspicious hai — real life mein fraud karne wale jaldi-jaldi multiple transactions try karte hain.

**24. What is the Location Rule?**
User ke "usual location" ko unke naye transaction ki location se compare karta hai. Agar mismatch hai (jaise user Delhi mein rehta hai par transaction "Unknown-VPN" se aaya), to flag hota hai.

**25. Why is the amount threshold fixed instead of dynamic?**
Simplicity ke liye — beginner-level project mein ek fixed threshold (jaise ₹5000) samajhna aur explain karna easy hai. Advanced version mein yeh per-user average ke basis par dynamic ho sakta hai.

**26. What does the DetectionResult class do?**
Yeh ek result object hai jo batata hai ki transaction flag hui ya nahi, kitne rules trigger hue, aur kya reasons the. Isse `FraudDetectionService` clean tarike se result return kar pata hai.

**27. How does the live simulation work?**
`TransactionSimulator` class `Runnable` implement karti hai. Jab "Start Simulation" button dabate hain, ek naya `Thread` bankar yeh class background mein chalti hai — har 3 second mein ek random transaction generate karti hai, database mein save karti hai, aur fraud check chalati hai.

**28. Why use SwingUtilities.invokeLater()?**
Swing thread-safe nahi hai — sirf Event Dispatch Thread (EDT) hi GUI components ko safely update kar sakta hai. Jab background thread (simulator) se GUI update karna hota hai, `invokeLater()` us update ko EDT par schedule kar deta hai.

**29. What is the bonus ML classifier and how is it different from the rules?**
Yeh Weka library ka `J48` decision tree hai, jo sample training data (`training_data.arff`) se seekh kar khud decide karta hai ki transaction fraud hai ya nahi — rules ki tarah hum manually condition nahi likhte, model data se pattern seekhta hai. Yeh optional hai, core app iske bina bhi chalta hai.

**30. What would you improve if you had more time?**
JUnit se unit tests likhta, passwords ko properly hash karta (BCrypt), aur amount threshold ko dynamic (per-user average ke basis par) bana deta.

---

## Extra: Two-Sided Design (Bank + Customer)

**31. Why does the app have two separate entry points (Main and CustomerMain)?**
Real fraud detection systems have do अलग audiences — bank ke fraud analysts (jo sab transactions dekhte hain) aur customers khud (jo sirf apna data dekh sakte hain). Do alag apps banane se yeh separation naturally dikhta hai, aur customer kabhi bhi kisi aur ka data nahi dekh sakta.

**32. How do the two apps share data if they're separate programs?**
Dono apps same MySQL database se connect hote hain (`fraud_detection`). `AppContext` class dono mein same DAO aur detection rules use karti hai, taaki dono apps mein logic consistent rahe. Jab customer app se payment hoti hai, woh turant database mein save hoti hai — bank app refresh karne par wahi data dikhta hai.

**33. Why is there a login only on the customer side, not the bank side?**
Yeh ek scoping decision hai jo maine explicitly document kiya hai — bank side ek internal ops tool maan ke banaya hai (jahan trusted staff already andar hain), jabki customer side external users ke liye hai jinke data ko isolate karna zaroori hai. Real system mein bank side bhi properly authenticated hota, par is project ke scope mein maine focus customer-side isolation par rakha.

**34. Is the password storage secure?**
Nahi, aur maine isko explicitly comment mein likha hai — password plain text mein store hota hai is project mein, sirf simplicity ke liye. Real system mein BCrypt jaisa salted hashing algorithm use hota, jisse raw password kabhi database mein store hi nahi hota.

**35. What happens if a customer denies a transaction ("This wasn't me")?**
Transaction ka status "BLOCKED" ho jaata hai (`transactionDAO.updateStatus()` call hoti hai). Real system mein yeh customer ka card turant freeze kar deta aur ek fraud investigation case khol deta — is prototype mein hum sirf status update karte hain taaki concept dikhe.
