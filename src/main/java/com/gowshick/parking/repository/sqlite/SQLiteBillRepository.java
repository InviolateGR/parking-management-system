package com.gowshick.parking.repository.sqlite;

import com.gowshick.parking.enums.PaymentMethod;
import com.gowshick.parking.enums.VehicleType;
import com.gowshick.parking.factory.VehicleFactory;
import com.gowshick.parking.model.*;
import com.gowshick.parking.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteBillRepository implements BillRepository {

    private static final Logger logger = LoggerFactory.getLogger(SQLiteBillRepository.class);

    private final DatabaseInitializer db;
    private final ParkingLot parkingLot;

    public SQLiteBillRepository(DatabaseInitializer db, ParkingLot parkingLot) {
        this.db = db;
        this.parkingLot = parkingLot;
    }

    @Override
    public void save(Bill bill) {
        String sql = """
            INSERT INTO bills (bill_id, ticket_id, registration_number, vehicle_type, slot_id, entry_time, exit_time, amount, payment_method)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Ticket ticket = bill.getTicket();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bill.getBillId());
            stmt.setString(2, ticket.getTicketId());
            stmt.setString(3, ticket.getVehicle().getRegistrationNumber());
            stmt.setString(4, ticket.getVehicle().getVehicleType().name());
            stmt.setString(5, ticket.getSlot().getSlotId());
            stmt.setString(6, ticket.getEntryTime().toString());
            stmt.setString(7, bill.getExitTime().toString());
            stmt.setDouble(8, bill.getAmount());
            stmt.setString(9, bill.getPaymentMethod().name());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Failed to save bill {} to database", bill.getBillId(), e);
            throw new RuntimeException("Could not persist bill", e);
        }
    }

    @Override
    public Optional<Bill> findById(String billId) {
        String sql = "SELECT * FROM bills WHERE bill_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, billId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Failed to find bill {} in database", billId, e);
            throw new RuntimeException("Could not query bill", e);
        }
    }

    @Override
    public List<Bill> findAll() {
        String sql = "SELECT * FROM bills";
        List<Bill> bills = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                bills.add(mapRow(rs));
            }

        } catch (SQLException e) {
            logger.error("Failed to fetch all bills from database", e);
            throw new RuntimeException("Could not query bills", e);
        }

        return bills;
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        String billId = rs.getString("bill_id");
        String ticketId = rs.getString("ticket_id");
        String regNumber = rs.getString("registration_number");
        VehicleType vehicleType = VehicleType.valueOf(rs.getString("vehicle_type"));
        String slotId = rs.getString("slot_id");
        LocalDateTime entryTime = LocalDateTime.parse(rs.getString("entry_time"));
        LocalDateTime exitTime = LocalDateTime.parse(rs.getString("exit_time"));
        double amount = rs.getDouble("amount");
        PaymentMethod paymentMethod = PaymentMethod.valueOf(rs.getString("payment_method"));

        Vehicle vehicle = VehicleFactory.createVehicle(vehicleType, regNumber);
        ParkingSlot slot = parkingLot.findSlotById(slotId)
                .orElseThrow(() -> new IllegalStateException(
                        "Referenced slot " + slotId + " not found in parking lot"));

        Ticket ticket = new Ticket(ticketId, vehicle, slot, entryTime);
        return new Bill(billId, ticket, exitTime, amount, paymentMethod);
    }
}