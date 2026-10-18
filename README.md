# 🍳 GourmetHub — Online Recipe Sharing Platform

A feature-rich Java Swing desktop application integrated with MySQL via the Data Access Object (DAO) pattern. GourmetHub provides role-based authentication, real-time aggregate rating calculations, persistent recipe bookmarking, and transactional admin moderation queues.

---

## 🌟 Key Features

* **Role-Based Access Control (RBAC):** Separate user interfaces and permissions for regular **Users** (browsing, bookmarking, posting reviews, submitting recipes) and **System Admins** (moderation queues, KPI analytics).
* **Cryptographic Password Security:** Authentication protected with **SHA-256 password hashing** using Java's native `java.security.MessageDigest`.
* **Dynamic Card Grid UI:** Scalable 3-column Swing grid with custom badge styling, ingredient checklists, step-by-step modals, and filtering by search query, cuisine, and dietary tags.
* **Real-Time Database Ratings:** Automated mathematical average (`AVG(rating)`) calculated dynamically from the `reviews` table and refreshed instantly across the main UI.
* **Persistent Collections:** Instant bookmarking system backed by the `favorites` relational table.
* **Multithreaded Analytics Engine:** Secondary background worker thread (`RecipeAnalyticsThread`) processing active recipe counts and average preparation metrics without locking the Swing Event Dispatch Thread (EDT).
* **ACID-Compliant Bulk Transactions:** Admin moderation queue utilizing manual JDBC transaction batching (`conn.setAutoCommit(false)`) with explicit `commit()` and `rollback()` error safety.

---

## 🛠️ Technology Stack

* **Language:** Java 17+
* **GUI Framework:** Java Swing (`System Look & Feel`)
* **Database:** MySQL 8.0 Server
* **Connectivity:** JDBC (`mysql-connector-j`)
* **Architecture Pattern:** Data Access Object (DAO)

---

## 🚀 Getting Started

### 1. Database Setup
1. Launch **MySQL Workbench** (or your preferred MySQL client).
2. Open and execute the included setup script [`RecipeSharing_schema.sql`](./RecipeSharing_schema.sql) to initialize `recipe_sharing_db`, foreign key tables, and seed data:

```sql
SOURCE RecipeSharing_schema.sql;
