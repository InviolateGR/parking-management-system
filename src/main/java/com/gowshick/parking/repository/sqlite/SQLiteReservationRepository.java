package com.gowshick.parking.repository.sqlite;

import com.gowshick.parking.enums.VehicleType;
import com.gowshick.parking.factory.VehicleFactory;
import com.gowshick.parking.model.ParkingLot;
import com.gowshick.parking.model.ParkingSlot;
import com.gowshick.parking.model.Reservation;
import com.gowshick.parking.model.Vehicle;
import com.gowshick.parking.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteReservationRepository implements ReservationRepository {

    private static final Logger logger = LoggerFactory.getLogger(SQLiteReservationRepository.class);

    private final DatabaseInitializer db;
    private final ParkingLot parkingLot;

    public SQLiteReservationRepository(DatabaseInitializer db, ParkingLot parkingLot) {
        this.db = db;
        this.parkingLot = parkingLot;
    }

    @Override
    public void save(Reservation reservation) {
        String sql = """
            INSERT INTO reservations (reservation_id, registration_number, vehicle_type, slot_id, start_time, end_time, active)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservation.getReservationId());
            stmt.setString(2, reservation.getVehicle().getRegistrationNumber());
            stmt.setString(3, reservation.getVehicle().getVehicleType().name());
            stmt.setString(4, reservation.getSlot().getSlotId());
            stmt.setString(5, reservation.getStartTime().toString());
            stmt.setString(6, reservation.getEndTime().toString());
            stmt.setInt(7, reservation.isActive() ? 1 : 0);

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Failed to save reservation {} to database", reservation.getReservationId(), e);
            throw new RuntimeException("Could not persist reservation", e);
        }
    }

    @Override
    public Optional<Reservation> findById(String reservationId) {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservationId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Failed to find reservation {} in database", reservationId, e);
            throw new RuntimeException("Could not query reservation", e);
        }
    }

    @Override
    public List<Reservation> findAll() {
        String sql = "SELECT * FROM reservations";
        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reservations.add(mapRow(rs));
            }

        } catch (SQLException e) {
            logger.error("Failed to fetch all reservations from database", e);
            throw new RuntimeException("Could not query reservations", e);
        }

        return reservations;
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        String reservationId = rs.getString("reservation_id");
        String regNumber = rs.getString("registration_number");
        VehicleType vehicleType = VehicleType.valueOf(rs.getString("vehicle_type"));
        String slotId = rs.getString("slot_id");
        LocalDateTime startTime = LocalDateTime.parse(rs.getString("start_time"));
        LocalDateTime endTime = LocalDateTime.parse(rs.getString("end_time"));
        boolean active = rs.getInt("active") == 1;

        Vehicle vehicle = VehicleFactory.createVehicle(vehicleType, regNumber);
        ParkingSlot slot = parkingLot.findSlotById(slotId)
                .orElseThrow(() -> new IllegalStateException(
                        "Referenced slot " + slotId + " not found in parking lot"));

        Reservation reservation = new Reservation(reservationId, vehicle, slot, startTime, endTime);
        if (!active) {
            reservation.deactivate();
        }
        return reservation;
    }
}