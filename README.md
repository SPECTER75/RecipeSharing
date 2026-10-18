# 🍳 GourmetHub — Multi-Threaded Recipe Sharing & Moderation Platform

An enterprise-grade, desktop culinary platform built using Java Swing and MySQL. GourmetHub demonstrates core Object-Oriented Programming (OOP) principles, multi-threaded GUI analytics, transactional database moderation, and dynamic SQL aggregate computations.

---

## 📐 Project Directory Structure

```text
RecipeSharing/
├── .idea/                      # IDE Workspace Configuration
├── src/
│   └── main/
│       └── java/
│           └── org/
│               └── example/
│                   └── RecipeAppGUI.java   # Main GUI Application & DAO Controllers
├── RecipeSharing_schema.sql    # Standalone Database DDL & Seed Script
├── .gitignore                  # Git Build Artifact Exclusions
├── pom.xml                     # Maven Dependencies & Build Configuration
└── README.md                   # System Documentation

```

---

## 🌟 Key Functional Features

### 1. Authentication & Security

* **Role-Based Access Control (RBAC):** Distinct privileges for **Users** (recipe submission, rating, bookmarking) and **Admins** (review queues, bulk transaction approvals).
* **Cryptographic Security:** Hashing of user credentials using SHA-256 via Java's native `java.security.MessageDigest`.

### 2. Interactive GUI & Recipe Grid

* **Dynamic Grid Layout:** Non-stretching 3-column card grid built on Swing `GridLayout` and `CardLayout` panels.
* **Search & Multi-Filter Engine:** Real-time filtering across titles, ingredients, cuisines (Indian, Mexican, Italian, Asian), and dietary preferences.
* **Modal Dialogs:** Detailed views featuring interactive ingredient checklists, step-by-step instructions, and review submission dialogs.

### 3. Real-Time Aggregate Ratings

* **SQL Aggregation:** Recipe cards calculate and render average star ratings dynamically via database aggregate queries (`SELECT AVG(rating) FROM reviews WHERE recipe_id = ?`).
* **Background UI Refresh:** Submitting a review triggers immediate execution of `globalRefreshCards.run()`, refreshing card metrics across the UI without restarting the application.

### 4. Saved Collections & Bookmarks

* **Persistent Favorites:** One-click toggling (❤️) mapped directly to a relational `favorites` database table with immediate cross-tab synchronization.

### 5. Multi-Threaded Analytics Engine

* **Non-Blocking EDT Execution:** A background thread (`RecipeAnalyticsThread`) computes live system metrics asynchronously, preserving Swing UI responsiveness.

### 6. Transactional Moderation Queue

* **ACID Compliance:** Admin bulk-approvals utilize explicit JDBC manual transaction batching (`conn.setAutoCommit(false)`) with rollback safety.

---

## 🏗️ Academic OOP Architecture & Design Patterns

| OOP / Software Concept | Implementation in GourmetHub |
| --- | --- |
| **Data Access Object (DAO)** | Decouples Swing UI components from MySQL interactions (`UserDAO`, `RecipeDAO`, `ReviewDAO`, `FavoriteDAO`). |
| **Inheritance & Polymorphism** | Abstract base class `User` extended by specialized `RegularUser` and `AdminUser` subclasses. |
| **Encapsulation** | Domain objects wrap state with private attributes and type-checked getters/setters. |
| **Interface Abstraction** | Generic `RecipeOperations<T>` interface establishing contract operations across repository layers. |
| **Concurrency Control** | Background worker thread extending `java.lang.Thread` for synchronized analytics rendering. |
| **SQL Injection Prevention** | All raw queries utilize parametric `PreparedStatement` interfaces. |

---

## 📊 Database Schema Design

The application relies on a relational schema defined in `RecipeSharing_schema.sql`:

```sql
users (user_id PK, name, email UNIQUE, password, role)
recipes (recipe_id PK, author_id FK, title, ingredients, instructions, cuisine, approval_status)
reviews (review_id PK, recipe_id FK, user_id FK, user_name, rating, comment)
favorites (favorite_id PK, user_id FK, recipe_id FK, UNIQUE(user_id, recipe_id))

```

---

## 🚀 Setup & Installation Guide

### Prerequisites

* **Java Development Kit (JDK):** Version 17 or higher
* **Database:** MySQL Server 8.0+ & MySQL Workbench
* **Build Tool:** Apache Maven 3.8+

### 1. Database Initialization

Open MySQL Workbench and execute the included setup script:

```sql
SOURCE C:/PROJECTS/Himanshu/RecipeSharing/RecipeSharing_schema.sql;

```

### 2. Database Connection Credentials

Verify or update connection parameters inside `RecipeAppGUI.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/recipe_sharing_db";
private static final String USER = "root";
private static final String PASSWORD = "YOUR_MYSQL_PASSWORD";

```

### 3. Build & Run

Run via Maven from terminal:

```bash
mvn clean compile exec:java -Dexec.mainClass="org.example.RecipeAppGUI"

```

---

## 🔑 Default Seed Credentials

| Role | Email | Plaintext Password | SHA-256 Hash Stored |
| --- | --- | --- | --- |
| **Regular User** | `user@recipe.com` | `user123` | `ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f` |
| **Admin Executive** | `admin@recipe.com` | `admin123` | `8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918` |

---

## 👤 Author & Attribution

* **Developer:** Sanidhya Singh Chauhan ([SPECTER75](https://www.google.com/search?q=https://github.com/SPECTER75))
* **Project Name:** GourmetHub / RecipeSharing Platform

```

Once pasted, click the green **Commit changes...** button at the top right to save it to your repository.

<FollowUp>
Would you like me to help you verify the rendered README or move on to preparing your presentation outline for Review 1?
</FollowUp>

```
