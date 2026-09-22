package com.gowshick.parking.repository.sqlite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final String jdbcUrl;

    public DatabaseInitializer(String dbFilePath) {
        this.jdbcUrl = "jdbc:sqlite:" + dbFilePath;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(jdbcUrl);
        } catch (SQLException e) {
            logger.error("Failed to connect to database", e);
            throw new RuntimeException("Could not connect to database", e);
        }
    }

    public void initializeSchema() {
        String createTickets = """
            CREATE TABLE IF NOT EXISTS tickets (
                ticket_id TEXT PRIMARY KEY,
                registration_number TEXT NOT NULL,
                vehicle_type TEXT NOT NULL,
                slot_id TEXT NOT NULL,
                entry_time TEXT NOT NULL
            )
            """;

        String createBills = """
            CREATE TABLE IF NOT EXISTS bills (
                bill_id TEXT PRIMARY KEY,
                ticket_id TEXT NOT NULL,
                registration_number TEXT NOT NULL,
                vehicle_type TEXT NOT NULL,
                slot_id TEXT NOT NULL,
                entry_time TEXT NOT NULL,
                exit_time TEXT NOT NULL,
                amount REAL NOT NULL,
                payment_method TEXT NOT NULL
            )
            """;

        String createReservations = """
            CREATE TABLE IF NOT EXISTS reservations (
                reservation_id TEXT PRIMARY KEY,
                registration_number TEXT NOT NULL,
                vehicle_type TEXT NOT NULL,
                slot_id TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT NOT NULL,
                active INTEGER NOT NULL
            )
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTickets);
            stmt.execute(createBills);
            stmt.execute(createReservations);

            logger.info("Database schema initialized successfully");

        } catch (SQLException e) {
            logger.error("Failed to initialize database schema", e);
            throw new RuntimeException("Could not initialize schema", e);
        }
    }
}