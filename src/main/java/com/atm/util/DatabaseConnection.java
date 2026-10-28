package com.atm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Simple JDBC connection pool for MySQL.
 * Uses a blocking queue to manage connections.
 * All SQL operations MUST use prepared statements.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private final BlockingQueue<Connection> connectionPool;
    private final String url;
    private final String username;
    private final String password;
    private final int poolSize;
    private boolean initialized = false;

    private DatabaseConnection() {
        ConfigManager config = ConfigManager.getInstance();
        this.url = config.getDbUrl();
        this.username = config.getDbUsername();
        this.password = config.getDbPassword();
        this.poolSize = config.getInt("db.pool.maxSize", 10);
        this.connectionPool = new ArrayBlockingQueue<>(poolSize);
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Initializes the connection pool. Must be called once at startup.
     */
    public synchronized void initialize() throws SQLException {
        if (initialized) return;

        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Ensure mysql-connector-j is on the classpath.", e);
        }

        int minIdle = ConfigManager.getInstance().getInt("db.pool.minIdle", 2);
        for (int i = 0; i < minIdle; i++) {
            connectionPool.offer(createConnection());
        }
        initialized = true;
        System.out.println("[DB] Connection pool initialized with " + minIdle + " connections.");
    }

    private Connection createConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(true); // Default; services explicitly manage transactions
        return conn;
    }

    /**
     * Gets a connection from the pool. Creates a new one if the pool is empty
     * and the limit hasn't been reached.
     */
    public Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }

        Connection conn = connectionPool.poll();
        if (conn != null && !conn.isClosed() && conn.isValid(2)) {
            return conn;
        }
        // Create a new connection if pool is empty or connection was stale
        return createConnection();
    }

    /**
     * Returns a connection to the pool for reuse.
     */
    public void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed() && conn.isValid(2)) {
                    // Reset auto-commit in case it was changed
                    if (!conn.getAutoCommit()) {
                        conn.setAutoCommit(true);
                    }
                    if (!connectionPool.offer(conn)) {
                        conn.close(); // Pool full, close the connection
                    }
                } else {
                    conn.close();
                }
            } catch (SQLException e) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Closes all connections in the pool and shuts down.
     */
    public synchronized void shutdown() {
        Connection conn;
        while ((conn = connectionPool.poll()) != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
        initialized = false;
        System.out.println("[DB] Connection pool shut down.");
    }

    /**
     * Tests if the database is reachable.
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            boolean valid = conn.isValid(5);
            releaseConnection(conn);
            return valid;
        } catch (SQLException e) {
            return false;
        }
    }
}
