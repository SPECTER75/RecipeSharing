-- ========================================================
-- GourmetHub Database Schema & Initial Seed Data
-- ========================================================

CREATE DATABASE IF NOT EXISTS recipe_sharing_db;
USE recipe_sharing_db;

-- Disable FK checks for clean reset during setup
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS recipes;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. USERS TABLE
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(256) NOT NULL,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. RECIPES TABLE
CREATE TABLE recipes (
    recipe_id INT AUTO_INCREMENT PRIMARY KEY,
    author_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    ingredients TEXT NOT NULL,
    instructions TEXT NOT NULL,
    cuisine VARCHAR(50) NOT NULL,
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. REVIEWS & RATINGS TABLE
CREATE TABLE reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    recipe_id INT NOT NULL,
    user_id INT NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. FAVORITES / BOOKMARKS TABLE
CREATE TABLE favorites (
    favorite_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    recipe_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_recipe (user_id, recipe_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id) ON DELETE CASCADE
);

-- ========================================================
-- SEED DATA
-- ========================================================

-- Default Accounts (Passwords: user123 & admin123)
INSERT INTO users (user_id, name, email, password, role) VALUES
(1, 'Test User', 'user@recipe.com', 'user123', 'USER'),
(2, 'System Executive Admin', 'admin@recipe.com', 'admin123', 'ADMIN');

-- Sample Approved Recipes
INSERT INTO recipes (recipe_id, author_id, title, ingredients, instructions, cuisine, approval_status) VALUES
(1, 1, 'Paneer Butter Masala', 'Paneer 250g, Tomato puree 1 cup, Butter 2 tbsp, Heavy cream 2 tbsp, Garam masala 1 tsp, Ginger-garlic paste 1 tbsp, Salt, Kasuri methi', 'Heat butter, add ginger-garlic paste, tomato puree, and spices. Simmer for 10 mins. Add paneer cubes and cream.', 'Indian', 'APPROVED'),
(2, 1, 'Fresh Guacamole', 'Ripe avocados 3, Lime juice 2 tbsp, Finely chopped onion 1/2, Chopped cilantro 2 tbsp, Salt 1/2 tsp, Chopped jalapeño 1', 'Mash avocados with lime juice and salt. Fold in chopped onion, cilantro, and jalapeño. Serve immediately.', 'Mexican', 'APPROVED'),
(3, 1, 'Classic Creamy Carbonara', 'Spaghetti 200g, Pancetta 100g, Eggs 2, Pecorino cheese 50g, Black pepper', 'Boil pasta in salted water. Fry pancetta until crispy. Whisk eggs and grated cheese in a bowl. Toss hot pasta with pancetta, remove from heat, quickly stir in egg mixture to create a creamy sauce.', 'Italian', 'APPROVED'),
(4, 1, 'Vegetable Thai Green Curry', 'Coconut milk 400ml, Green curry paste 2 tbsp, Mixed veggies 2 cups, Tofu 150g, Thai basil, Soy sauce 1 tbsp, Salt', 'Heat curry paste in a pan, pour in coconut milk and bring to simmer. Add veggies and tofu, cook for 10 mins. Season with soy sauce and garnish with fresh Thai basil.', 'Asian', 'APPROVED');