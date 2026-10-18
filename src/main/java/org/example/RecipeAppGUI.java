package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// ==========================================
// 1. CONFIGURATION & EXCEPTION HANDLING
// ==========================================
class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/recipe_sharing_db";
    private static final String USER = "root";
    private static final String PASSWORD = "SAN@07"; // Saved MySQL password

    // Always returns a fresh, independent connection to prevent nested ResultSet collisions
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database Driver Not Found!", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

class RecipeException extends Exception {
    public RecipeException(String message) { super(message); }
    public RecipeException(String message, Throwable cause) { super(message, cause); }
}

interface RecipeOperations<T> {
    boolean add(T entity) throws RecipeException;
    boolean updateStatus(int id, String status) throws RecipeException;
    List<T> getAllApproved() throws RecipeException;
}

// ==========================================
// 2. OOP DOMAIN MODELS
// ==========================================
abstract class User {
    protected int userId;
    protected String name;
    protected String email;
    protected String password;
    protected String role;

    public User(int userId, String name, String email, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public abstract String getRoleTitle();

    public int getUserId() { return userId; }
    public String getName() { return name != null ? name : ""; }
    public String getEmail() { return email != null ? email : ""; }
    public String getRole() { return role != null ? role : "USER"; }
}

class RegularUser extends User {
    public RegularUser(int userId, String name, String email, String password) {
        super(userId, name, email, password, "USER");
    }
    @Override public String getRoleTitle() { return "Culinary Hub & Explorer"; }
}

class AdminUser extends User {
    public AdminUser(int userId, String name, String email, String password) {
        super(userId, name, email, password, "ADMIN");
    }
    @Override public String getRoleTitle() { return "System Executive Portal"; }
}

class Recipe {
    private int recipeId;
    private int authorId;
    private String title = "";
    private String ingredients = "";
    private String instructions = "";
    private String cuisine = "";
    private String approvalStatus = "PENDING";
    private int cookTimeMins = 30;
    private String difficulty = "Medium";
    private String dietaryTag = "Vegetarian";
    private double rating = 4.5;
    private int calories = 350;

    public Recipe(int authorId, String title, String ingredients, String instructions, String cuisine, int cookTimeMins, String difficulty, String dietaryTag, int calories) {
        this.authorId = authorId;
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.cuisine = cuisine;
        this.cookTimeMins = cookTimeMins;
        this.difficulty = difficulty;
        this.dietaryTag = dietaryTag;
        this.calories = calories;
        this.approvalStatus = "PENDING";
    }

    public Recipe() {}

    public int getRecipeId() { return recipeId; }
    public void setRecipeId(int recipeId) { this.recipeId = recipeId; }
    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }
    public String getTitle() { return title != null ? title : ""; }
    public void setTitle(String title) { this.title = title; }
    public String getIngredients() { return ingredients != null ? ingredients : ""; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getInstructions() { return instructions != null ? instructions : ""; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getCuisine() { return cuisine != null ? cuisine : ""; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public String getApprovalStatus() { return approvalStatus != null ? approvalStatus : "PENDING"; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public int getCookTimeMins() { return cookTimeMins; }
    public void setCookTimeMins(int cookTimeMins) { this.cookTimeMins = cookTimeMins; }
    public String getDifficulty() { return difficulty != null ? difficulty : "Medium"; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getDietaryTag() { return dietaryTag != null ? dietaryTag : "Vegetarian"; }
    public void setDietaryTag(String dietaryTag) { this.dietaryTag = dietaryTag; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
}

class Review {
    private int reviewId;
    private int recipeId;
    private int userId;
    private String userName;
    private int rating;
    private String comment;

    public Review(int recipeId, int userId, String userName, int rating, String comment) {
        this.recipeId = recipeId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
    }

    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }
    public int getRecipeId() { return recipeId; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName != null ? userName : "User"; }
    public int getRating() { return rating; }
    public String getComment() { return comment != null ? comment : ""; }
}

// ==========================================
// 3. MULTITHREADING ANALYTICS ENGINE
// ==========================================
class RecipeAnalyticsThread extends Thread {
    private final List<Recipe> recipes;
    private static final Object lock = new Object();
    private static String summary = "System Ready";

    public RecipeAnalyticsThread(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    @Override
    public void run() {
        synchronized (lock) {
            long total = recipes.size();
            Map<String, Long> cuisines = recipes.stream().collect(Collectors.groupingBy(Recipe::getCuisine, Collectors.counting()));
            double avgCook = recipes.stream().mapToInt(Recipe::getCookTimeMins).average().orElse(0.0);
            summary = String.format(" ⚡ Analytics Engine: %d Active Recipes Across %d Cuisines | Avg Cook Time: %.0f mins", total, cuisines.size(), avgCook);
        }
    }

    public static String getSummary() { return summary; }
}

// ==========================================
// 4. DATA ACCESS OBJECTS (SECURITY & JDBC)
// ==========================================
class SecurityUtil {
    public static String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return input;
        }
    }
}

class UserDAO {
    public User authenticate(String email, String password) throws RecipeException {
        String hashedPassword = SecurityUtil.hashSHA256(password);
        String sql = "SELECT * FROM users WHERE email = ? AND (password = ? OR password = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                if ("ADMIN".equalsIgnoreCase(role)) {
                    return new AdminUser(rs.getInt("user_id"), rs.getString("name"), email, password);
                } else {
                    return new RegularUser(rs.getInt("user_id"), rs.getString("name"), email, password);
                }
            }
        } catch (SQLException e) {
            throw new RecipeException("Authentication error", e);
        }
        return null;
    }

    public boolean registerUser(String name, String email, String password) throws RecipeException {
        String hashedPassword = SecurityUtil.hashSHA256(password);
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, 'USER')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new RecipeException("Email already registered! Try logging in.", e);
            }
            throw new RecipeException("Registration failed due to database error.", e);
        }
    }
}

class FavoriteDAO {
    public boolean addFavorite(int userId, int recipeId) {
        String sql = "INSERT IGNORE INTO favorites (user_id, recipe_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, recipeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean removeFavorite(int userId, int recipeId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND recipe_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, recipeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public Set<Integer> getFavoriteIds(int userId) {
        Set<Integer> favorites = new HashSet<>();
        String sql = "SELECT recipe_id FROM favorites WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                favorites.add(rs.getInt("recipe_id"));
            }
        } catch (SQLException ignored) {}
        return favorites;
    }
}

class ReviewDAO {
    public boolean addReview(Review review) throws RecipeException {
        String sql = "INSERT INTO reviews (recipe_id, user_id, user_name, rating, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, review.getRecipeId());
            stmt.setInt(2, review.getUserId());
            stmt.setString(3, review.getUserName());
            stmt.setInt(4, review.getRating());
            stmt.setString(5, review.getComment());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RecipeException("Could not post review.", e);
        }
    }

    public List<Review> getReviewsForRecipe(int recipeId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE recipe_id = ? ORDER BY review_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recipeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Review rev = new Review(
                        rs.getInt("recipe_id"),
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getInt("rating"),
                        rs.getString("comment")
                );
                rev.setReviewId(rs.getInt("review_id"));
                list.add(rev);
            }
        } catch (SQLException ignored) {}
        return list;
    }

    public double getAverageRating(int recipeId) {
        String sql = "SELECT AVG(rating) FROM reviews WHERE recipe_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recipeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : avg;
            }
        } catch (SQLException ignored) {}
        return 0.0;
    }
}

class RecipeDAO implements RecipeOperations<Recipe> {
    private final ReviewDAO reviewDAO = new ReviewDAO();

    @Override
    public boolean add(Recipe recipe) throws RecipeException {
        String sql = "INSERT INTO recipes (author_id, title, ingredients, instructions, cuisine, approval_status) VALUES (?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recipe.getAuthorId());
            stmt.setString(2, recipe.getTitle());
            stmt.setString(3, recipe.getIngredients());
            stmt.setString(4, recipe.getInstructions());
            stmt.setString(5, recipe.getCuisine());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RecipeException("Error adding recipe", e);
        }
    }

    @Override
    public boolean updateStatus(int recipeId, String status) throws RecipeException {
        String sql = "UPDATE recipes SET approval_status = ? WHERE recipe_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, recipeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RecipeException("Error updating recipe status", e);
        }
    }

    public boolean bulkApprove(List<Integer> recipeIds) throws RecipeException {
        String sql = "UPDATE recipes SET approval_status = 'APPROVED' WHERE recipe_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int id : recipeIds) {
                    stmt.setInt(1, id);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw new RecipeException("Bulk approval transaction failed. Rolled back.", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @Override
    public List<Recipe> getAllApproved() throws RecipeException {
        return getRecipesByQuery("SELECT * FROM recipes WHERE approval_status = 'APPROVED'");
    }

    public List<Recipe> getPending() throws RecipeException {
        return getRecipesByQuery("SELECT * FROM recipes WHERE approval_status = 'PENDING'");
    }

    public List<Recipe> getRecipesByAuthor(int authorId) throws RecipeException {
        return getRecipesByQuery("SELECT * FROM recipes WHERE author_id = " + authorId);
    }

    private List<Recipe> getRecipesByQuery(String sql) throws RecipeException {
        List<Recipe> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Recipe r = new Recipe();
                r.setRecipeId(rs.getInt("recipe_id"));
                r.setAuthorId(rs.getInt("author_id"));
                r.setTitle(rs.getString("title"));
                r.setIngredients(rs.getString("ingredients"));
                r.setInstructions(rs.getString("instructions"));
                r.setCuisine(rs.getString("cuisine"));
                r.setApprovalStatus(rs.getString("approval_status"));

                r.setCookTimeMins(20 + (r.getRecipeId() * 7) % 35);
                r.setDifficulty(r.getRecipeId() % 2 == 0 ? "Easy" : "Medium");
                r.setDietaryTag(r.getCuisine().equalsIgnoreCase("Italian") ? "Non-Veg" : "Vegetarian");
                r.setCalories(280 + (r.getRecipeId() * 45) % 300);

                double dbAvg = reviewDAO.getAverageRating(r.getRecipeId());
                if (dbAvg > 0) {
                    r.setRating(dbAvg);
                } else {
                    r.setRating(4.5);
                }

                list.add(r);
            }
        } catch (SQLException e) {
            throw new RecipeException("Error reading recipes", e);
        }
        return list;
    }
}

// ==========================================
// 5. MAIN SWING APPLICATION PLATFORM
// ==========================================
public class RecipeAppGUI extends JFrame {
    private User currentUser;
    private final RecipeDAO recipeDAO = new RecipeDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();
    private Set<Integer> userFavorites = new HashSet<>();
    private Runnable globalRefreshCards = () -> {};

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    private static final Font FONT_EMOJI_LARGE = new Font("Segoe UI Emoji", Font.PLAIN, 18);
    private static final Font FONT_EMOJI_NORMAL = new Font("Segoe UI Emoji", Font.PLAIN, 13);

    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private static final Color COLOR_ACCENT = new Color(79, 70, 229);
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);
    private static final Color COLOR_DARK_BG = new Color(15, 23, 42);
    private static final Color COLOR_CARD_BG = new Color(255, 255, 255);
    private static final Color COLOR_APP_BG = new Color(241, 245, 249);
    private static final Color COLOR_TEXT_MAIN = new Color(30, 41, 59);

    public RecipeAppGUI() {
        setTitle("GourmetHub | Online Recipe Sharing Platform");
        setSize(1150, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createRegisterPanel(), "REGISTER");
        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_DARK_BG);
        header.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel title = new JLabel("🍳 GourmetHub | Online Recipe Sharing Platform");
        title.setFont(FONT_EMOJI_LARGE);
        title.setForeground(Color.WHITE);

        JPanel userSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userSection.setOpaque(false);

        JLabel userInfo = new JLabel("👤 " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        userInfo.setFont(FONT_EMOJI_NORMAL);
        userInfo.setForeground(new Color(226, 232, 240));

        JButton logoutBtn = createStyledButton("Logout", COLOR_DANGER);
        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));

        userSection.add(userInfo);
        userSection.add(logoutBtn);

        header.add(title, BorderLayout.WEST);
        header.add(userSection, BorderLayout.EAST);
        return header;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_APP_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));

        JLabel titleLabel = new JLabel("🍳 GourmetHub");
        titleLabel.setFont(FONT_EMOJI_LARGE);
        titleLabel.setForeground(COLOR_PRIMARY);

        JLabel subLabel = new JLabel("Sign in to discover, share, and rate recipes");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(Color.GRAY);

        JTextField emailField = new JTextField("user@recipe.com", 22);
        JPasswordField passField = new JPasswordField("user123", 22);
        styleInputField(emailField);
        styleInputField(passField);

        JButton loginBtn = createStyledButton("Sign In", COLOR_PRIMARY);
        JButton registerSwitchBtn = createStyledButton("Create New Account", COLOR_DARK_BG);

        GridBagConstraints cgbc = new GridBagConstraints();
        cgbc.insets = new Insets(6, 8, 6, 8);
        cgbc.gridx = 0; cgbc.gridy = 0; cgbc.gridwidth = 2;
        card.add(titleLabel, cgbc);

        cgbc.gridy = 1;
        card.add(subLabel, cgbc);

        cgbc.gridwidth = 1; cgbc.gridy = 2;
        card.add(new JLabel("Email Address:"), cgbc);
        cgbc.gridx = 1;
        card.add(emailField, cgbc);

        cgbc.gridx = 0; cgbc.gridy = 3;
        card.add(new JLabel("Password:"), cgbc);
        cgbc.gridx = 1;
        card.add(passField, cgbc);

        cgbc.gridx = 0; cgbc.gridy = 4; cgbc.gridwidth = 2;
        card.add(loginBtn, cgbc);

        cgbc.gridy = 5;
        card.add(registerSwitchBtn, cgbc);

        panel.add(card, gbc);

        loginBtn.addActionListener(e -> {
            try {
                User user = userDAO.authenticate(emailField.getText().trim(), new String(passField.getPassword()).trim());
                if (user != null) {
                    this.currentUser = user;
                    this.userFavorites = favoriteDAO.getFavoriteIds(currentUser.getUserId());
                    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                        mainPanel.add(createAdminPanel(), "ADMIN");
                        cardLayout.show(mainPanel, "ADMIN");
                    } else {
                        mainPanel.add(createUserPanel(), "USER");
                        cardLayout.show(mainPanel, "USER");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Email or Password!", "Auth Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (RecipeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerSwitchBtn.addActionListener(e -> cardLayout.show(mainPanel, "REGISTER"));

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_APP_BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JLabel titleLabel = new JLabel("Create Free Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(COLOR_TEXT_MAIN);

        JTextField nameField = new JTextField(22);
        JTextField emailField = new JTextField(22);
        JPasswordField passField = new JPasswordField(22);
        styleInputField(nameField);
        styleInputField(emailField);
        styleInputField(passField);

        JButton registerBtn = createStyledButton("Register Account (SHA-256)", COLOR_SUCCESS);
        JButton backBtn = createStyledButton("Back to Login", COLOR_DARK_BG);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(titleLabel, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1;
        card.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        card.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        card.add(new JLabel("Email Address:"), gbc);
        gbc.gridx = 1;
        card.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        card.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        card.add(passField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        card.add(registerBtn, gbc);

        gbc.gridy = 5;
        card.add(backBtn, gbc);

        panel.add(card);

        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword()).trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                if (userDAO.registerUser(name, email, pass)) {
                    JOptionPane.showMessageDialog(this, "Registration Successful! SHA-256 encrypted account created.");
                    nameField.setText(""); emailField.setText(""); passField.setText("");
                    cardLayout.show(mainPanel, "LOGIN");
                }
            } catch (RecipeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));

        return panel;
    }

    private JPanel createUserPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // TAB 1: DISCOVER RECIPES GRID
        JPanel discoverPanel = new JPanel(new BorderLayout(15, 15));
        discoverPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        discoverPanel.setBackground(COLOR_APP_BG);

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filterBar.setBackground(COLOR_CARD_BG);
        filterBar.setBorder(new LineBorder(new Color(226, 232, 240), 1));

        JTextField searchFld = new JTextField(16);
        styleInputField(searchFld);

        JComboBox<String> cuisineFilter = new JComboBox<>(new String[]{"All Cuisines", "Indian", "Italian", "Mexican", "Asian", "American"});
        JComboBox<String> dietaryFilter = new JComboBox<>(new String[]{"All Diets", "Vegetarian", "Non-Veg", "Vegan"});
        JButton exportCsvBtn = createStyledButton("📁 Export CSV", COLOR_DARK_BG);

        JLabel searchIconLbl = new JLabel("🔍 Search:");
        searchIconLbl.setFont(FONT_EMOJI_NORMAL);

        filterBar.add(searchIconLbl);
        filterBar.add(searchFld);
        filterBar.add(new JLabel("Cuisine:"));
        filterBar.add(cuisineFilter);
        filterBar.add(new JLabel("Diet:"));
        filterBar.add(dietaryFilter);
        filterBar.add(exportCsvBtn);

        // Cards Grid Container Clamped Top
        JPanel cardsGridContainer = new JPanel(new GridLayout(0, 3, 16, 16));
        cardsGridContainer.setBackground(COLOR_APP_BG);

        JPanel cardsGridWrapper = new JPanel(new BorderLayout());
        cardsGridWrapper.setBackground(COLOR_APP_BG);
        cardsGridWrapper.add(cardsGridContainer, BorderLayout.NORTH);

        JScrollPane cardsScrollPane = new JScrollPane(cardsGridWrapper);
        cardsScrollPane.setBorder(null);
        cardsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JLabel statusBar = new JLabel(" System Ready", JLabel.LEFT);
        statusBar.setFont(FONT_EMOJI_NORMAL);
        statusBar.setForeground(COLOR_DARK_BG);
        statusBar.setBorder(new EmptyBorder(8, 4, 4, 4));

        discoverPanel.add(filterBar, BorderLayout.NORTH);
        discoverPanel.add(cardsScrollPane, BorderLayout.CENTER);
        discoverPanel.add(statusBar, BorderLayout.SOUTH);

        Runnable renderCards = () -> {
            try {
                cardsGridContainer.removeAll();
                List<Recipe> recipes = recipeDAO.getAllApproved();

                String q = searchFld.getText().trim().toLowerCase();
                String selCuisine = cuisineFilter.getSelectedItem() != null ? cuisineFilter.getSelectedItem().toString() : "All Cuisines";
                String selDiet = dietaryFilter.getSelectedItem() != null ? dietaryFilter.getSelectedItem().toString() : "All Diets";

                List<Recipe> filtered = recipes.stream().filter(r -> {
                    String title = r.getTitle().toLowerCase();
                    String ing = r.getIngredients().toLowerCase();
                    String cuis = r.getCuisine();
                    String diet = r.getDietaryTag();

                    boolean matchesSearch = q.isEmpty() || title.contains(q) || ing.contains(q);
                    boolean matchesCuisine = selCuisine.equals("All Cuisines") || cuis.equalsIgnoreCase(selCuisine);
                    boolean matchesDiet = selDiet.equals("All Diets") || diet.equalsIgnoreCase(selDiet);
                    return matchesSearch && matchesCuisine && matchesDiet;
                }).collect(Collectors.toList());

                for (Recipe r : filtered) {
                    cardsGridContainer.add(createRecipeCardPanel(r));
                }

                cardsGridContainer.revalidate();
                cardsGridContainer.repaint();

                RecipeAnalyticsThread analytics = new RecipeAnalyticsThread(recipes);
                analytics.start();
                try { analytics.join(50); } catch (InterruptedException ignored) {}
                statusBar.setText(RecipeAnalyticsThread.getSummary());

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error rendering recipe cards:\n" + ex.getMessage(), "Render Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        this.globalRefreshCards = renderCards;

        searchFld.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { renderCards.run(); }
            public void removeUpdate(DocumentEvent e) { renderCards.run(); }
            public void changedUpdate(DocumentEvent e) { renderCards.run(); }
        });

        cuisineFilter.addActionListener(e -> renderCards.run());
        dietaryFilter.addActionListener(e -> renderCards.run());
        exportCsvBtn.addActionListener(e -> exportApprovedRecipesToCSV());

        // TAB 2: SAVED FAVORITES COLLECTION
        JPanel favoritesPanel = new JPanel(new BorderLayout(15, 15));
        favoritesPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        favoritesPanel.setBackground(COLOR_APP_BG);

        JPanel favGridContainer = new JPanel(new GridLayout(0, 3, 16, 16));
        favGridContainer.setBackground(COLOR_APP_BG);

        JPanel favGridWrapper = new JPanel(new BorderLayout());
        favGridWrapper.setBackground(COLOR_APP_BG);
        favGridWrapper.add(favGridContainer, BorderLayout.NORTH);

        JScrollPane favScrollPane = new JScrollPane(favGridWrapper);
        favScrollPane.setBorder(null);

        favoritesPanel.add(new JLabel(" ❤️ Saved Recipe Collection", JLabel.LEFT), BorderLayout.NORTH);
        favoritesPanel.add(favScrollPane, BorderLayout.CENTER);

        Runnable renderFavorites = () -> {
            try {
                favGridContainer.removeAll();
                userFavorites = favoriteDAO.getFavoriteIds(currentUser.getUserId());
                List<Recipe> all = recipeDAO.getAllApproved();
                List<Recipe> favs = all.stream().filter(r -> userFavorites.contains(r.getRecipeId())).collect(Collectors.toList());

                if (favs.isEmpty()) {
                    JLabel emptyLbl = new JLabel("No bookmarked recipes yet! Click ❤️ on any card to save it here.", JLabel.CENTER);
                    emptyLbl.setFont(FONT_EMOJI_NORMAL);
                    favGridContainer.add(emptyLbl);
                } else {
                    for (Recipe r : favs) {
                        favGridContainer.add(createRecipeCardPanel(r));
                    }
                }
                favGridContainer.revalidate();
                favGridContainer.repaint();
            } catch (Exception ex) { ex.printStackTrace(); }
        };

        // TAB 3: SUBMIT NEW RECIPE FORM
        JPanel submitForm = new JPanel(new GridBagLayout());
        submitForm.setBackground(COLOR_APP_BG);

        JPanel submitCard = new JPanel(new GridBagLayout());
        submitCard.setBackground(COLOR_CARD_BG);
        submitCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleFld = new JTextField(25);
        JTextField cuisineFld = new JTextField(25);
        JTextArea ingFld = new JTextArea(4, 25);
        JTextArea instFld = new JTextArea(4, 25);
        styleInputField(titleFld);
        styleInputField(cuisineFld);
        ingFld.setLineWrap(true); instFld.setLineWrap(true);

        JButton submitBtn = createStyledButton("Submit Recipe for Moderation", COLOR_PRIMARY);

        gbc.gridx = 0; gbc.gridy = 0; submitCard.add(new JLabel("Recipe Title:"), gbc);
        gbc.gridx = 1; submitCard.add(titleFld, gbc);

        gbc.gridx = 0; gbc.gridy = 1; submitCard.add(new JLabel("Cuisine Type:"), gbc);
        gbc.gridx = 1; submitCard.add(cuisineFld, gbc);

        gbc.gridx = 0; gbc.gridy = 2; submitCard.add(new JLabel("Ingredients List:"), gbc);
        gbc.gridx = 1; submitCard.add(new JScrollPane(ingFld), gbc);

        gbc.gridx = 0; gbc.gridy = 3; submitCard.add(new JLabel("Cooking Instructions:"), gbc);
        gbc.gridx = 1; submitCard.add(new JScrollPane(instFld), gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        submitCard.add(submitBtn, gbc);

        submitForm.add(submitCard);

        submitBtn.addActionListener(e -> {
            if (titleFld.getText().trim().isEmpty() || cuisineFld.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and Cuisine are required fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Recipe r = new Recipe(currentUser.getUserId(), titleFld.getText().trim(), ingFld.getText().trim(), instFld.getText().trim(), cuisineFld.getText().trim(), 25, "Medium", "Vegetarian", 320);
                if (recipeDAO.add(r)) {
                    JOptionPane.showMessageDialog(this, "Recipe submitted successfully! Awaiting Admin Approval.");
                    titleFld.setText(""); cuisineFld.setText(""); ingFld.setText(""); instFld.setText("");
                }
            } catch (RecipeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Submission Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // TAB 4: MY SUBMISSIONS
        JPanel myPanel = new JPanel(new BorderLayout(10, 10));
        myPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        myPanel.setBackground(COLOR_APP_BG);

        DefaultTableModel myModel = new DefaultTableModel(new String[]{"ID", "Title", "Cuisine", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable myTable = new JTable(myModel);
        styleTable(myTable);

        Runnable loadMySubmissions = () -> {
            try {
                myModel.setRowCount(0);
                for (Recipe r : recipeDAO.getRecipesByAuthor(currentUser.getUserId())) {
                    myModel.addRow(new Object[]{r.getRecipeId(), r.getTitle(), r.getCuisine(), r.getApprovalStatus()});
                }
            } catch (RecipeException ex) {
                ex.printStackTrace();
            }
        };

        myPanel.add(new JLabel(" Track Live Status of Your Contributions", JLabel.LEFT), BorderLayout.NORTH);
        myPanel.add(new JScrollPane(myTable), BorderLayout.CENTER);

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 0) renderCards.run();
            if (tabbedPane.getSelectedIndex() == 1) renderFavorites.run();
            if (tabbedPane.getSelectedIndex() == 3) loadMySubmissions.run();
        });

        tabbedPane.addTab(" Discover Recipes Grid", discoverPanel);
        tabbedPane.addTab(" ❤️ Saved Collection", favoritesPanel);
        tabbedPane.addTab(" Submit Recipe", submitForm);
        tabbedPane.addTab(" My Contributions", myPanel);

        JPanel root = new JPanel(new BorderLayout());
        root.add(createHeaderPanel(), BorderLayout.NORTH);
        root.add(tabbedPane, BorderLayout.CENTER);

        renderCards.run(); // Initial load

        return root;
    }

    private JPanel createRecipeCardPanel(Recipe r) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        card.setPreferredSize(new Dimension(320, 270));

        // Header Banner with Bookmark Toggle
        JPanel visualBanner = new JPanel(new BorderLayout());
        visualBanner.setBackground(COLOR_PRIMARY);
        visualBanner.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel bannerTitle = new JLabel("🍳 " + r.getTitle());
        bannerTitle.setFont(FONT_EMOJI_NORMAL);
        bannerTitle.setForeground(Color.WHITE);

        boolean isFav = userFavorites.contains(r.getRecipeId());
        JButton favBtn = new JButton(isFav ? "❤️" : "🤍");
        favBtn.setFont(FONT_EMOJI_NORMAL);
        favBtn.setContentAreaFilled(false);
        favBtn.setBorderPainted(false);
        favBtn.setFocusPainted(false);
        favBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        favBtn.addActionListener(e -> {
            if (userFavorites.contains(r.getRecipeId())) {
                favoriteDAO.removeFavorite(currentUser.getUserId(), r.getRecipeId());
                userFavorites.remove(r.getRecipeId());
                favBtn.setText("🤍");
            } else {
                favoriteDAO.addFavorite(currentUser.getUserId(), r.getRecipeId());
                userFavorites.add(r.getRecipeId());
                favBtn.setText("❤️");
            }
        });

        visualBanner.add(bannerTitle, BorderLayout.WEST);
        visualBanner.add(favBtn, BorderLayout.EAST);

        // Badges Row
        JPanel topHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topHeader.setOpaque(false);

        JLabel cuisineBadge = new JLabel(" " + r.getCuisine().toUpperCase() + " ");
        cuisineBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        cuisineBadge.setOpaque(true);
        cuisineBadge.setBackground(new Color(219, 234, 254));
        cuisineBadge.setForeground(COLOR_PRIMARY);

        JLabel dietBadge = new JLabel(" " + r.getDietaryTag().toUpperCase() + " ");
        dietBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        dietBadge.setOpaque(true);
        dietBadge.setBackground(new Color(220, 252, 231));
        dietBadge.setForeground(COLOR_SUCCESS);

        topHeader.add(cuisineBadge);
        topHeader.add(dietBadge);

        // Center Body Content
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JLabel ratingLbl = new JLabel(String.format("⭐ %.1f / 5.0 Rating", r.getRating()));
        ratingLbl.setFont(FONT_EMOJI_NORMAL);
        ratingLbl.setForeground(new Color(217, 119, 6));

        JLabel metaLbl = new JLabel(String.format("⏱️ %d mins  |  🔥 %d kcal  |  %s", r.getCookTimeMins(), r.getCalories(), r.getDifficulty()));
        metaLbl.setFont(FONT_EMOJI_NORMAL);
        metaLbl.setForeground(new Color(71, 85, 105));

        JTextArea snippet = new JTextArea("Ingredients: " + r.getIngredients());
        snippet.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        snippet.setForeground(Color.GRAY);
        snippet.setLineWrap(true);
        snippet.setWrapStyleWord(true);
        snippet.setEditable(false);
        snippet.setOpaque(false);
        snippet.setBorder(new EmptyBorder(4, 0, 4, 0));

        body.add(topHeader);
        body.add(Box.createVerticalStrut(4));
        body.add(ratingLbl);
        body.add(Box.createVerticalStrut(2));
        body.add(metaLbl);
        body.add(Box.createVerticalStrut(4));
        body.add(snippet);

        // Bottom Action Button
        JButton viewBtn = createStyledButton("View Full Recipe & Steps", COLOR_PRIMARY);
        viewBtn.addActionListener(e -> showInteractiveRecipeDialog(r));

        card.add(visualBanner, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(viewBtn, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(COLOR_APP_BG);

        // KPI Cards
        JPanel kpiGrid = new JPanel(new GridLayout(1, 3, 15, 15));
        kpiGrid.setOpaque(false);

        JLabel kpi1 = createKPICard("Pending Approval", "Moderation Queue", COLOR_PRIMARY);
        JLabel kpi2 = createKPICard("Transactional Safety", "ACID Compliant DAO", COLOR_SUCCESS);
        JLabel kpi3 = createKPICard("Access Control", "Role-Based RBAC", COLOR_ACCENT);

        kpiGrid.add(kpi1); kpiGrid.add(kpi2); kpiGrid.add(kpi3);

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Author ID", "Title", "Cuisine", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton approveBtn = createStyledButton("Approve Selected", COLOR_SUCCESS);
        JButton rejectBtn = createStyledButton("Reject Selected", COLOR_DANGER);
        JButton bulkApproveBtn = createStyledButton("Bulk Approve All (Transaction)", COLOR_PRIMARY);
        JButton exportCsvBtn = createStyledButton("📁 Export Queue to CSV", COLOR_DARK_BG);

        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);
        btnPanel.add(bulkApproveBtn);
        btnPanel.add(exportCsvBtn);

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(new JLabel(" Pending Moderation Queue", JLabel.LEFT), BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(kpiGrid, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        Runnable loadPending = () -> {
            try {
                model.setRowCount(0);
                List<Recipe> pending = recipeDAO.getPending();
                for (Recipe r : pending) {
                    model.addRow(new Object[]{r.getRecipeId(), r.getAuthorId(), r.getTitle(), r.getCuisine(), r.getApprovalStatus()});
                }
            } catch (RecipeException ex) {
                ex.printStackTrace();
            }
        };

        loadPending.run();

        approveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int) model.getValueAt(row, 0);
                try {
                    recipeDAO.updateStatus(id, "APPROVED");
                    loadPending.run();
                } catch (RecipeException ex) { ex.printStackTrace(); }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a recipe row first!");
            }
        });

        rejectBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int) model.getValueAt(row, 0);
                try {
                    recipeDAO.updateStatus(id, "REJECTED");
                    loadPending.run();
                } catch (RecipeException ex) { ex.printStackTrace(); }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a recipe row first!");
            }
        });

        bulkApproveBtn.addActionListener(e -> {
            try {
                List<Integer> ids = new ArrayList<>();
                for (int i = 0; i < model.getRowCount(); i++) {
                    ids.add((int) model.getValueAt(i, 0));
                }
                if (!ids.isEmpty()) {
                    recipeDAO.bulkApprove(ids);
                    JOptionPane.showMessageDialog(this, "Transaction Committed: All pending recipes bulk approved!");
                    loadPending.run();
                } else {
                    JOptionPane.showMessageDialog(this, "No pending recipes to approve.");
                }
            } catch (RecipeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        exportCsvBtn.addActionListener(e -> exportTableToCSV(table, "Pending_Moderation_Report.csv"));

        JPanel root = new JPanel(new BorderLayout());
        root.add(createHeaderPanel(), BorderLayout.NORTH);
        root.add(panel, BorderLayout.CENTER);

        return root;
    }

    private JLabel createKPICard(String value, String subtitle, Color color) {
        JLabel card = new JLabel("<html><div style='text-align: center;'><b>" + value + "</b><br><font size='2' color='gray'>" + subtitle + "</font></div></html>", JLabel.CENTER);
        card.setOpaque(true);
        card.setBackground(COLOR_CARD_BG);
        card.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color, 2, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
        return card;
    }

    private void showInteractiveRecipeDialog(Recipe r) {
        JDialog dialog = new JDialog(this, "Recipe Details - " + r.getTitle(), true);
        dialog.setSize(650, 560);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(COLOR_CARD_BG);

        JLabel titleLbl = new JLabel("<html><h2>🍳 " + r.getTitle() + "</h2><font color='gray'>" + r.getCuisine() + " • ⏱️ " + r.getCookTimeMins() + " Mins • 🔥 " + r.getCalories() + " kcal</font></html>");
        titleLbl.setFont(FONT_EMOJI_NORMAL);
        titleLbl.setForeground(COLOR_PRIMARY);

        JTabbedPane detailTabs = new JTabbedPane();

        // TAB 1: Checkbox Checklist
        JPanel ingPanel = new JPanel();
        ingPanel.setLayout(new BoxLayout(ingPanel, BoxLayout.Y_AXIS));
        ingPanel.setBackground(COLOR_CARD_BG);

        for (String ing : r.getIngredients().split(",")) {
            JCheckBox cb = new JCheckBox(ing.trim());
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cb.setBackground(COLOR_CARD_BG);
            ingPanel.add(cb);
        }

        // TAB 2: Instructions
        JTextArea instArea = new JTextArea(r.getInstructions());
        instArea.setEditable(false); instArea.setLineWrap(true); instArea.setWrapStyleWord(true);
        instArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // TAB 3: Reviews & Ratings
        JPanel reviewsPanel = new JPanel(new BorderLayout(10, 10));
        reviewsPanel.setBackground(COLOR_CARD_BG);

        DefaultListModel<String> reviewListModel = new DefaultListModel<>();
        JList<String> reviewList = new JList<>(reviewListModel);
        reviewList.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        Runnable loadReviews = () -> {
            reviewListModel.clear();
            List<Review> revs = reviewDAO.getReviewsForRecipe(r.getRecipeId());
            if (revs.isEmpty()) {
                reviewListModel.addElement("No reviews yet. Be the first to leave a review below!");
            } else {
                for (Review rev : revs) {
                    reviewListModel.addElement(String.format("⭐ %d/5 by %s: %s", rev.getRating(), rev.getUserName(), rev.getComment()));
                }
            }
        };

        loadReviews.run();

        JPanel writeReviewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        writeReviewPanel.setOpaque(false);

        JComboBox<Integer> ratingBox = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        JTextField commentFld = new JTextField(20);
        JButton postReviewBtn = createStyledButton("Post Review", COLOR_PRIMARY);

        writeReviewPanel.add(new JLabel("Rating:"));
        writeReviewPanel.add(ratingBox);
        writeReviewPanel.add(new JLabel("Comment:"));
        writeReviewPanel.add(commentFld);
        writeReviewPanel.add(postReviewBtn);

        postReviewBtn.addActionListener(e -> {
            String comment = commentFld.getText().trim();
            if (comment.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a review comment!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int rating = (int) ratingBox.getSelectedItem();
                Review newRev = new Review(r.getRecipeId(), currentUser.getUserId(), currentUser.getName(), rating, comment);
                if (reviewDAO.addReview(newRev)) {
                    JOptionPane.showMessageDialog(dialog, "Review posted successfully!");
                    commentFld.setText("");
                    loadReviews.run();
                    globalRefreshCards.run();
                }
            } catch (RecipeException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Review Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        reviewsPanel.add(new JScrollPane(reviewList), BorderLayout.CENTER);
        reviewsPanel.add(writeReviewPanel, BorderLayout.SOUTH);

        detailTabs.addTab("Ingredients Checklist", new JScrollPane(ingPanel));
        detailTabs.addTab("Preparation Steps", new JScrollPane(instArea));
        detailTabs.addTab("⭐ Reviews & Ratings", reviewsPanel);

        JButton closeBtn = createStyledButton("Close Recipe", COLOR_DARK_BG);
        closeBtn.addActionListener(e -> dialog.dispose());

        panel.add(titleLbl, BorderLayout.NORTH);
        panel.add(detailTabs, BorderLayout.CENTER);
        panel.add(closeBtn, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void exportApprovedRecipesToCSV() {
        try {
            List<Recipe> list = recipeDAO.getAllApproved();
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("Approved_Recipes.csv"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File target = chooser.getSelectedFile();
                try (PrintWriter writer = new PrintWriter(new FileWriter(target))) {
                    writer.println("Recipe_ID,Title,Cuisine,Cook_Time_Mins,Calories,Ingredients,Instructions");
                    for (Recipe r : list) {
                        writer.printf("\"%d\",\"%s\",\"%s\",\"%d\",\"%d\",\"%s\",\"%s\"\n",
                                r.getRecipeId(), r.getTitle(), r.getCuisine(), r.getCookTimeMins(), r.getCalories(),
                                r.getIngredients().replace("\"", "\"\""), r.getInstructions().replace("\"", "\"\""));
                    }
                    JOptionPane.showMessageDialog(this, "Exported successfully to:\n" + target.getAbsolutePath());
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportTableToCSV(JTable table, String defaultFileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultFileName));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File targetFile = chooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
                for (int col = 0; col < table.getColumnCount(); col++) {
                    writer.print("\"" + table.getColumnName(col) + "\"" + (col == table.getColumnCount() - 1 ? "" : ","));
                }
                writer.println();
                for (int row = 0; row < table.getRowCount(); row++) {
                    for (int col = 0; col < table.getColumnCount(); col++) {
                        Object val = table.getValueAt(row, col);
                        String str = val != null ? val.toString().replace("\"", "\"\"") : "";
                        writer.print("\"" + str + "\"" + (col == table.getColumnCount() - 1 ? "" : ","));
                    }
                    writer.println();
                }
                JOptionPane.showMessageDialog(this, "Report exported successfully to:\n" + targetFile.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export Error: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_EMOJI_NORMAL);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleInputField(JTextField fld) {
        fld.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fld.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(6, 8, 6, 8)
        ));
    }

    private void styleTable(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(226, 232, 240));
        table.getTableHeader().setForeground(COLOR_TEXT_MAIN);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(COLOR_TEXT_MAIN);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new RecipeAppGUI().setVisible(true));
    }
}