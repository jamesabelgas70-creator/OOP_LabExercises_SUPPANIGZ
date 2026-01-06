package com.aidsync.util;

import java.sql.*;
import java.io.File;

/**
 * Manages database connection and initialization
 */
public class DatabaseManager {
    private static final String DB_NAME = "aidsync.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_NAME;
    private static Connection connection;

    /**
     * Initialize database and create tables if they don't exist
     */
    public static void initialize() {
        try {
            // Create database file if it doesn't exist
            File dbFile = new File(DB_NAME);
            if (!dbFile.exists()) {
                dbFile.createNewFile();
            }
            
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            
            createTables();
            initializeData();
            
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error creating database file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get database connection
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    /**
     * Create all required tables
     */
    private static void createTables() throws SQLException {
        // Users table
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "username TEXT UNIQUE NOT NULL, " +
            "password TEXT NOT NULL, " +
            "full_name TEXT, " +
            "email TEXT, " +
            "phone TEXT, " +
            "role TEXT NOT NULL CHECK(role IN ('Admin', 'Staff')), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "last_login TIMESTAMP)";
        
        // Beneficiaries table
        String createBeneficiariesTable = "CREATE TABLE IF NOT EXISTS beneficiaries (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "beneficiary_id TEXT UNIQUE NOT NULL, " +
            "full_name TEXT NOT NULL, " +
            "birth_date DATE, " +
            "gender TEXT, " +
            "contact_number TEXT, " +
            "barangay TEXT NOT NULL, " +
            "purok TEXT NOT NULL, " +
            "street_address TEXT, " +
            "family_size INTEGER NOT NULL CHECK(family_size >= 1 AND family_size <= 20), " +
            "is_household_head INTEGER DEFAULT 0, " +
            "is_pwd INTEGER DEFAULT 0, " +
            "is_senior_citizen INTEGER DEFAULT 0, " +
            "is_pregnant INTEGER DEFAULT 0, " +
            "is_solo_parent INTEGER DEFAULT 0, " +
            "date_registered TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "status TEXT DEFAULT 'Active' CHECK(status IN ('Active', 'Inactive')), " +
            "deleted INTEGER DEFAULT 0, " +
            "deleted_at TIMESTAMP, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        
        // Inventory table
        String createInventoryTable = "CREATE TABLE IF NOT EXISTS inventory (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "item_name TEXT UNIQUE NOT NULL, " +
            "category TEXT, " +
            "quantity INTEGER NOT NULL DEFAULT 0 CHECK(quantity >= 0), " +
            "unit TEXT, " +
            "low_stock_threshold INTEGER DEFAULT 10, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        
        // Calamities table
        String createCalamitiesTable = "CREATE TABLE IF NOT EXISTS calamities (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT UNIQUE NOT NULL, " +
            "description TEXT, " +
            "status TEXT DEFAULT 'Active' CHECK(status IN ('Active', 'Inactive')), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        
        // Calamity items table (many-to-many: calamity -> inventory items)
        String createCalamityItemsTable = "CREATE TABLE IF NOT EXISTS calamity_items (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "calamity_id INTEGER NOT NULL, " +
            "inventory_id INTEGER NOT NULL, " +
            "standard_quantity INTEGER NOT NULL CHECK(standard_quantity > 0), " +
            "FOREIGN KEY (calamity_id) REFERENCES calamities(id) ON DELETE CASCADE, " +
            "FOREIGN KEY (inventory_id) REFERENCES inventory(id), " +
            "UNIQUE(calamity_id, inventory_id))";
        
        // Distributions table
        String createDistributionsTable = "CREATE TABLE IF NOT EXISTS distributions (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "beneficiary_id INTEGER NOT NULL, " +
            "calamity_id INTEGER, " +
            "distribution_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "distributed_by INTEGER NOT NULL, " +
            "notes TEXT, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id), " +
            "FOREIGN KEY (calamity_id) REFERENCES calamities(id), " +
            "FOREIGN KEY (distributed_by) REFERENCES users(id))";
        
        // Distribution items table (many-to-many relationship)
        String createDistributionItemsTable = "CREATE TABLE IF NOT EXISTS distribution_items (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "distribution_id INTEGER NOT NULL, " +
            "inventory_id INTEGER NOT NULL, " +
            "quantity INTEGER NOT NULL CHECK(quantity > 0), " +
            "FOREIGN KEY (distribution_id) REFERENCES distributions(id) ON DELETE CASCADE, " +
            "FOREIGN KEY (inventory_id) REFERENCES inventory(id))";
        
        // Activity log table
        String createActivityLogTable = "CREATE TABLE IF NOT EXISTS activity_log (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "user_id INTEGER, " +
            "action TEXT NOT NULL, " +
            "entity_type TEXT, " +
            "entity_id INTEGER, " +
            "details TEXT, " +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (user_id) REFERENCES users(id))";
        
        // Inventory transactions table (audit trail)
        String createInventoryTransactionsTable = "CREATE TABLE IF NOT EXISTS inventory_transactions (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "inventory_id INTEGER NOT NULL, " +
            "user_id INTEGER, " +
            "transaction_type TEXT NOT NULL CHECK(transaction_type IN ('Restock', 'Set Quantity', 'Distribution', 'Void Distribution')), " +
            "quantity_change INTEGER NOT NULL, " +
            "quantity_before INTEGER NOT NULL, " +
            "quantity_after INTEGER NOT NULL, " +
            "notes TEXT, " +
            "reference_id INTEGER, " +
            "reference_type TEXT, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (inventory_id) REFERENCES inventory(id), " +
            "FOREIGN KEY (user_id) REFERENCES users(id))";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createBeneficiariesTable);
            stmt.execute(createInventoryTable);
            stmt.execute(createCalamitiesTable);
            stmt.execute(createCalamityItemsTable);
            stmt.execute(createDistributionsTable);
            stmt.execute(createDistributionItemsTable);
            stmt.execute(createActivityLogTable);
            stmt.execute(createInventoryTransactionsTable);
            
            // Migrate existing users table to add new columns if they don't exist
            migrateUsersTable();
            
            // Migrate existing distributions table to add calamity_id if it doesn't exist
            migrateDistributionsTable();
            
            // Create indexes for better performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_beneficiaries_beneficiary_id ON beneficiaries(beneficiary_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_beneficiaries_name ON beneficiaries(full_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_beneficiaries_barangay ON beneficiaries(barangay)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_beneficiaries_status ON beneficiaries(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_distributions_beneficiary ON distributions(beneficiary_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_distributions_date ON distributions(distribution_date)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_distributions_calamity ON distributions(calamity_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_calamity_items_calamity ON calamity_items(calamity_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_calamity_items_inventory ON calamity_items(inventory_id)");
        }
    }

    /**
     * Migrate users table to add new columns if they don't exist
     */
    private static void migrateUsersTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Check if full_name column exists, if not add it
            try {
                stmt.executeQuery("SELECT full_name FROM users LIMIT 1");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                stmt.execute("ALTER TABLE users ADD COLUMN full_name TEXT");
            }
            
            // Check if email column exists, if not add it
            try {
                stmt.executeQuery("SELECT email FROM users LIMIT 1");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                stmt.execute("ALTER TABLE users ADD COLUMN email TEXT");
            }
            
            // Check if phone column exists, if not add it
            try {
                stmt.executeQuery("SELECT phone FROM users LIMIT 1");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                stmt.execute("ALTER TABLE users ADD COLUMN phone TEXT");
            }
        }
    }

    /**
     * Migrate distributions table to add calamity_id column if it doesn't exist
     */
    private static void migrateDistributionsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Check if calamity_id column exists, if not add it
            try {
                stmt.executeQuery("SELECT calamity_id FROM distributions LIMIT 1");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                stmt.execute("ALTER TABLE distributions ADD COLUMN calamity_id INTEGER");
                // Add foreign key constraint if possible (SQLite has limited ALTER TABLE support)
                // Note: SQLite doesn't support adding foreign keys via ALTER TABLE, so this is handled in CREATE TABLE
            }
        }
    }

    /**
     * Initialize default data (barangays, puroks, default admin user)
     */
    private static void initializeData() throws SQLException {
        // Check if data already exists
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM users")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Data already initialized
            }
        }
        
        // Create default admin user (username: admin, password: admin123)
        String defaultPassword = "admin123"; // In production, this should be hashed
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
            stmt.setString(1, "admin");
            stmt.setString(2, defaultPassword);
            stmt.setString(3, "Admin");
            stmt.executeUpdate();
        }
        
        // Initialize barangay and purok data (stored in BarangayData class)
        BarangayData.initializeBarangays(connection);
        
        // Initialize some default inventory items
        initializeDefaultInventory();
    }

    /**
     * Initialize default inventory items
     */
    private static void initializeDefaultInventory() throws SQLException {
        String[] defaultItems = {
            "INSERT OR IGNORE INTO inventory (item_name, category, quantity, unit, low_stock_threshold) VALUES ('Rice', 'Food', 100, 'kg', 20)",
            "INSERT OR IGNORE INTO inventory (item_name, category, quantity, unit, low_stock_threshold) VALUES ('Canned Goods', 'Food', 200, 'cans', 50)",
            "INSERT OR IGNORE INTO inventory (item_name, category, quantity, unit, low_stock_threshold) VALUES ('Water', 'Beverage', 150, 'bottles', 30)",
            "INSERT OR IGNORE INTO inventory (item_name, category, quantity, unit, low_stock_threshold) VALUES ('Blankets', 'Shelter', 50, 'pieces', 10)",
            "INSERT OR IGNORE INTO inventory (item_name, category, quantity, unit, low_stock_threshold) VALUES ('Hygiene Kit', 'Personal Care', 75, 'kits', 15)"
        };
        
        try (Statement stmt = connection.createStatement()) {
            for (String sql : defaultItems) {
                stmt.execute(sql);
            }
        }
    }

    /**
     * Close database connection
     */
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}

