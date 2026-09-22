package com.gowshick.parking.repository.sqlite;

import com.gowshick.parking.enums.VehicleType;
import com.gowshick.parking.factory.VehicleFactory;
import com.gowshick.parking.model.ParkingLot;
import com.gowshick.parking.model.ParkingSlot;
import com.gowshick.parking.model.Ticket;
import com.gowshick.parking.model.Vehicle;
import com.gowshick.parking.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteTicketRepository implements TicketRepository {

    private static final Logger logger = LoggerFactory.getLogger(SQLiteTicketRepository.class);

    private final DatabaseInitializer db;
    private final ParkingLot parkingLot;

    public SQLiteTicketRepository(DatabaseInitializer db, ParkingLot parkingLot) {
        this.db = db;
        this.parkingLot = parkingLot;
    }

    @Override
    public void save(Ticket ticket) {
        String sql = "INSERT INTO tickets (ticket_id, registration_number, vehicle_type, slot_id, entry_time) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ticket.getTicketId());
            stmt.setString(2, ticket.getVehicle().getRegistrationNumber());
            stmt.setString(3, ticket.getVehicle().getVehicleType().name());
            stmt.setString(4, ticket.getSlot().getSlotId());
            stmt.setString(5, ticket.getEntryTime().toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Failed to save ticket {} to database", ticket.getTicketId(), e);
            throw new RuntimeException("Could not persist ticket", e);
        }
    }

    @Override
    public Optional<Ticket> findById(String ticketId) {
        String sql = "SELECT * FROM tickets WHERE ticket_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ticketId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Failed to find ticket {} in database", ticketId, e);
            throw new RuntimeException("Could not query ticket", e);
        }
    }

    @Override
    public List<Ticket> findAll() {
        String sql = "SELECT * FROM tickets";
        List<Ticket> tickets = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tickets.add(mapRow(rs));
            }

        } catch (SQLException e) {
            logger.error("Failed to fetch all tickets from database", e);
            throw new RuntimeException("Could not query tickets", e);
        }

        return tickets;
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        String ticketId = rs.getString("ticket_id");
        String regNumber = rs.getString("registration_number");
        VehicleType vehicleType = VehicleType.valueOf(rs.getString("vehicle_type"));
        String slotId = rs.getString("slot_id");
        LocalDateTime entryTime = LocalDateTime.parse(rs.getString("entry_time"));

        Vehicle vehicle = VehicleFactory.createVehicle(vehicleType, regNumber);
        ParkingSlot slot = parkingLot.findSlotById(slotId)
                .orElseThrow(() -> new IllegalStateException(
                        "Referenced slot " + slotId + " not found in parking lot"));

        return new Ticket(ticketId, vehicle, slot, entryTime);
    }
}