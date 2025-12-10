package clinicmanager.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.sql.DataSource;


public class DatabaseConnection {
    private static DataSource dataSource;
    private static boolean useConnectionPool = false;
    private static boolean initialized = false;
    private static Properties dbProperties = new Properties();
    
    // Database configuration defaults
    private static String dbHost = "localhost";
    private static int dbPort = 5432;
    private static String dbName = "clinicmanager";
    private static String dbSchema = "public";
    private static String dbUsername = "postgres";
    private static String dbPassword = "postgres";

    // Load PostgreSQL driver and initialize connection pool
    static {
        loadDatabaseProperties();
        try {
            Class.forName("org.postgresql.Driver");
            initializeConnectionPool();
            initialized = true;
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found. Please add postgresql driver to your classpath.");
            System.err.println("Maven: <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId></dependency>");
            System.err.println("Or download from: https://jdbc.postgresql.org/download/");
        } catch (Exception e) {
            System.err.println("Error initializing database connection pool: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load database configuration from properties file or use defaults
     */
    private static void loadDatabaseProperties() {
        try {
            InputStream inputStream = DatabaseConnection.class.getResourceAsStream("/config/database.properties");
            if (inputStream == null) {
                inputStream = DatabaseConnection.class.getClassLoader().getResourceAsStream("config/database.properties");
            }
            if (inputStream == null) {
                // Try file system
                try {
                    java.io.File configFile = new java.io.File("config/database.properties");
                    if (configFile.exists()) {
                        inputStream = new java.io.FileInputStream(configFile);
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            if (inputStream != null) {
                dbProperties.load(inputStream);
                inputStream.close();
                
                // Load values from properties
                dbHost = dbProperties.getProperty("db.host", "localhost");
                dbPort = Integer.parseInt(dbProperties.getProperty("db.port", "5432"));
                dbName = dbProperties.getProperty("db.name", "clinicmanager");
                dbSchema = dbProperties.getProperty("db.schema", "public");
                dbUsername = dbProperties.getProperty("db.username", "postgres");
                dbPassword = dbProperties.getProperty("db.password", "yosra");
            } else {
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load database properties. Using defaults: " + e.getMessage());
        }
        
        // Override with environment variables if present
        String envHost = System.getenv("DB_HOST");
        if (envHost != null) dbHost = envHost;
        
        String envPort = System.getenv("DB_PORT");
        if (envPort != null) dbPort = Integer.parseInt(envPort);
        
        String envName = System.getenv("DB_NAME");
        if (envName != null) dbName = envName;
        
        String envUser = System.getenv("DB_USERNAME");
        if (envUser != null) dbUsername = envUser;
        
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null) dbPassword = envPass;
    }
    
    private static String getJdbcUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s?currentSchema=%s", dbHost, dbPort, dbName, dbSchema);
    }

    /**
     * Initialize HikariCP connection pool with enterprise configuration
     */
    private static void initializeConnectionPool() {
        try {
            // Use reflection to avoid compile-time dependency if HikariCP is not available
            Class<?> hikariConfigClass = Class.forName("com.zaxxer.hikari.HikariConfig");
            Class<?> hikariDataSourceClass = Class.forName("com.zaxxer.hikari.HikariDataSource");
            
            Object hikariConfig = hikariConfigClass.getDeclaredConstructor().newInstance();
            
            // Basic connection settings
            hikariConfigClass.getMethod("setJdbcUrl", String.class).invoke(hikariConfig, getJdbcUrl());
            hikariConfigClass.getMethod("setUsername", String.class).invoke(hikariConfig, dbUsername);
            hikariConfigClass.getMethod("setPassword", String.class).invoke(hikariConfig, dbPassword);
            
            // Connection pool settings
            int poolMin = Integer.parseInt(dbProperties.getProperty("db.pool.minimum", "5"));
            int poolMax = Integer.parseInt(dbProperties.getProperty("db.pool.maximum", "20"));
            long connTimeout = Long.parseLong(dbProperties.getProperty("db.pool.connectionTimeout", "30000"));
            long idleTimeout = Long.parseLong(dbProperties.getProperty("db.pool.idleTimeout", "600000"));
            long maxLifetime = Long.parseLong(dbProperties.getProperty("db.pool.maxLifetime", "1800000"));
            long leakThreshold = Long.parseLong(dbProperties.getProperty("db.pool.leakDetectionThreshold", "60000"));
            
            hikariConfigClass.getMethod("setMinimumIdle", int.class).invoke(hikariConfig, poolMin);
            hikariConfigClass.getMethod("setMaximumPoolSize", int.class).invoke(hikariConfig, poolMax);
            hikariConfigClass.getMethod("setConnectionTimeout", long.class).invoke(hikariConfig, connTimeout);
            hikariConfigClass.getMethod("setIdleTimeout", long.class).invoke(hikariConfig, idleTimeout);
            hikariConfigClass.getMethod("setMaxLifetime", long.class).invoke(hikariConfig, maxLifetime);
            hikariConfigClass.getMethod("setLeakDetectionThreshold", long.class).invoke(hikariConfig, leakThreshold);
            
            // PostgreSQL-specific optimizations
            hikariConfigClass.getMethod("addDataSourceProperty", String.class, String.class)
                .invoke(hikariConfig, "cachePrepStmts", "true");
            hikariConfigClass.getMethod("addDataSourceProperty", String.class, String.class)
                .invoke(hikariConfig, "prepStmtCacheSize", "250");
            hikariConfigClass.getMethod("addDataSourceProperty", String.class, String.class)
                .invoke(hikariConfig, "prepStmtCacheSqlLimit", "2048");
            hikariConfigClass.getMethod("addDataSourceProperty", String.class, String.class)
                .invoke(hikariConfig, "useServerPrepStmts", "true");
            hikariConfigClass.getMethod("addDataSourceProperty", String.class, String.class)
                .invoke(hikariConfig, "rewriteBatchedStatements", "true");
            
            // SSL configuration
            boolean sslEnabled = Boolean.parseBoolean(dbProperties.getProperty("db.ssl.enabled", "false"));
            if (sslEnabled) {
                hikariConfigClass.getMethod("addDataSourceProperty", String.class, String.class)
                    .invoke(hikariConfig, "ssl", "true");
                String sslMode = dbProperties.getProperty("db.ssl.mode", "prefer");
                hikariConfigClass.getMethod("addDataSourceProperty", String.class, String.class)
                    .invoke(hikariConfig, "sslmode", sslMode);
            }
            
            // Connection pool name for monitoring
            hikariConfigClass.getMethod("setPoolName", String.class).invoke(hikariConfig, "ClinicManagerPool");
            
            // Connection test query
            hikariConfigClass.getMethod("setConnectionTestQuery", String.class).invoke(hikariConfig, "SELECT 1");
            
            // Auto-commit
            hikariConfigClass.getMethod("setAutoCommit", boolean.class).invoke(hikariConfig, true);
            
            // Create data source
            dataSource = (DataSource) hikariDataSourceClass.getDeclaredConstructor(hikariConfigClass).newInstance(hikariConfig);
            useConnectionPool = true;
            
            // Initialize database schema
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("HikariCP not found. Using basic connection management.");
            System.err.println("For better performance, add HikariCP dependency.");
            System.err.println("Maven: <dependency><groupId>com.zaxxer</groupId><artifactId>HikariCP</artifactId></dependency>");
            // Fall back to basic connection management
            initializeBasicConnection();
        } catch (Exception e) {
            System.err.println("Failed to create connection pool: " + e.getMessage());
            e.printStackTrace();
            initializeBasicConnection();
        }
    }
    
   
    private static void initializeBasicConnection() {
        try {
            // Create a simple DataSource wrapper for basic connections
            dataSource = new BasicDataSource();
            useConnectionPool = false;
            initializeDatabase();
        } catch (Exception e) {
            System.err.println("Failed to initialize basic connection: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Simple DataSource implementation for basic connection management
     */
    private static class BasicDataSource implements DataSource {
        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(getJdbcUrl(), dbUsername, dbPassword);
        }
        
        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return DriverManager.getConnection(getJdbcUrl(), username, password);
        }
        
        @Override
        public java.io.PrintWriter getLogWriter() throws SQLException { return null; }
        @Override
        public void setLogWriter(java.io.PrintWriter out) throws SQLException {}
        @Override
        public void setLoginTimeout(int seconds) throws SQLException {}
        @Override
        public int getLoginTimeout() throws SQLException { return 0; }
        @Override
        public java.util.logging.Logger getParentLogger() { return null; }
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException { return null; }
        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException { return false; }
    }

    /**
     * Get a connection from the connection pool
     * @return Database connection
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized || dataSource == null) {
            throw new SQLException("Database connection pool not initialized. Check PostgreSQL driver and configuration.");
        }
        
        try {
            Connection connection = dataSource.getConnection();
            // Set schema if needed
            if (connection != null) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("SET search_path TO " + dbSchema);
                }
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Error obtaining database connection: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Initialize database schema - creates tables if they don't exist
     * Uses PostgreSQL-specific syntax
     */
    private static void initializeDatabase() {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {
            
            // Create Patients table with PostgreSQL syntax
            stmt.execute("CREATE TABLE IF NOT EXISTS patients (" +
                "id SERIAL PRIMARY KEY, " +
                "first_name VARCHAR(100) NOT NULL, " +
                "last_name VARCHAR(100) NOT NULL, " +
                "date_of_birth DATE, " +
                "phone_number VARCHAR(20), " +
                "email VARCHAR(255), " +
                "address TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");
            
            // Create Appointments table with PostgreSQL syntax
            stmt.execute("CREATE TABLE IF NOT EXISTS appointments (" +
                "id SERIAL PRIMARY KEY, " +
                "patient_id INTEGER NOT NULL, " +
                "appointment_date TIMESTAMP NOT NULL, " +
                "reason TEXT, " +
                "status VARCHAR(20) DEFAULT 'scheduled', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE" +
                ")");
            
            // Create Visits table with PostgreSQL syntax
            stmt.execute("CREATE TABLE IF NOT EXISTS visits (" +
                "id SERIAL PRIMARY KEY, " +
                "appointment_id INTEGER NOT NULL, " +
                "visit_date TIMESTAMP NOT NULL, " +
                "notes TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE" +
                ")");
            
            // Create indexes for better query performance
            createIndexes(stmt);
            
            // Create function to update updated_at timestamp
            createUpdateTimestampFunction(stmt);
            
            // Create triggers to automatically update updated_at
            createUpdateTriggers(stmt);
            
        } catch (SQLException e) {
            System.err.println("Error initializing database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create indexes for improved query performance
     */
    private static void createIndexes(Statement stmt) throws SQLException {
        // Index on patient names for faster searches
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_patients_name ON patients(last_name, first_name)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_patients_email ON patients(email)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_patients_phone ON patients(phone_number)");
        
        // Index on appointment dates for faster queries
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments(appointment_date)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_appointments_patient ON appointments(patient_id)");
        
        // Index on visit dates
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_visits_date ON visits(visit_date)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_visits_appointment ON visits(appointment_id)");
    }
    
    /**
     * Create PostgreSQL function to update timestamp
     */
    private static void createUpdateTimestampFunction(Statement stmt) throws SQLException {
        String functionSQL = "CREATE OR REPLACE FUNCTION update_updated_at_column() " +
            "RETURNS TRIGGER AS $$ " +
            "BEGIN " +
            "    NEW.updated_at = CURRENT_TIMESTAMP; " +
            "    RETURN NEW; " +
            "END; " +
            "$$ language 'plpgsql'";
        
        try {
            stmt.execute(functionSQL);
        } catch (SQLException e) {
            // Function might already exist, ignore
            if (!e.getMessage().contains("already exists")) {
                throw e;
            }
        }
    }
    
    /**
     * Create triggers to automatically update updated_at column
     */
    private static void createUpdateTriggers(Statement stmt) throws SQLException {
        // Trigger for patients table
        try {
            stmt.execute("DROP TRIGGER IF EXISTS update_patients_updated_at ON patients");
            stmt.execute("CREATE TRIGGER update_patients_updated_at " +
                "BEFORE UPDATE ON patients " +
                "FOR EACH ROW " +
                "EXECUTE FUNCTION update_updated_at_column()");
        } catch (SQLException e) {
            // Ignore if trigger already exists
        }
        
        // Trigger for appointments table
        try {
            stmt.execute("DROP TRIGGER IF EXISTS update_appointments_updated_at ON appointments");
            stmt.execute("CREATE TRIGGER update_appointments_updated_at " +
                "BEFORE UPDATE ON appointments " +
                "FOR EACH ROW " +
                "EXECUTE FUNCTION update_updated_at_column()");
        } catch (SQLException e) {
            // Ignore if trigger already exists
        }
        
        // Trigger for visits table
        try {
            stmt.execute("DROP TRIGGER IF EXISTS update_visits_updated_at ON visits");
            stmt.execute("CREATE TRIGGER update_visits_updated_at " +
                "BEFORE UPDATE ON visits " +
                "FOR EACH ROW " +
                "EXECUTE FUNCTION update_updated_at_column()");
        } catch (SQLException e) {
            // Ignore if trigger already exists
        }
    }

    /**
     * Close the connection pool and release all resources
     */
    public static void closeConnectionPool() {
        if (dataSource != null && useConnectionPool) {
            try {
                dataSource.getClass().getMethod("close").invoke(dataSource);
            } catch (Exception e) {
                System.err.println("Error closing connection pool: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get connection pool statistics for monitoring
     */
    public static String getPoolStats() {
        if (dataSource == null) {
            return "Connection pool not initialized";
        }
        
        if (!useConnectionPool) {
            return "Basic connection management (no pooling)";
        }
        
        try {
            // Use reflection to access HikariCP stats
            Object hikariPoolMXBean = dataSource.getClass().getMethod("getHikariPoolMXBean").invoke(dataSource);
            int active = (Integer) hikariPoolMXBean.getClass().getMethod("getActiveConnections").invoke(hikariPoolMXBean);
            int idle = (Integer) hikariPoolMXBean.getClass().getMethod("getIdleConnections").invoke(hikariPoolMXBean);
            int total = (Integer) hikariPoolMXBean.getClass().getMethod("getTotalConnections").invoke(hikariPoolMXBean);
            int waiting = (Integer) hikariPoolMXBean.getClass().getMethod("getThreadsAwaitingConnection").invoke(hikariPoolMXBean);
            String poolName = (String) dataSource.getClass().getMethod("getPoolName").invoke(dataSource);
            
            return String.format(
                "Pool: %s | Active: %d | Idle: %d | Total: %d | Waiting: %d",
                poolName, active, idle, total, waiting
            );
        } catch (Exception e) {
            return "Pool statistics unavailable: " + e.getMessage();
        }
    }
}
