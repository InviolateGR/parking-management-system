package com.gowshick.parking.repository.file;

import com.gowshick.parking.enums.VehicleType;
import com.gowshick.parking.factory.VehicleFactory;
import com.gowshick.parking.model.ParkingLot;
import com.gowshick.parking.model.ParkingSlot;
import com.gowshick.parking.model.Reservation;
import com.gowshick.parking.model.Vehicle;
import com.gowshick.parking.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileReservationRepository implements ReservationRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileReservationRepository.class);
    private static final String DELIMITER = "\\|";

    private final Path filePath;
    private final ParkingLot parkingLot;

    public FileReservationRepository(String filePath, ParkingLot parkingLot) {
        this.filePath = Paths.get(filePath);
        this.parkingLot = parkingLot;
    }

    @Override
    public void save(Reservation reservation) {
        String line = String.join("|",
                reservation.getReservationId(),
                reservation.getVehicle().getRegistrationNumber(),
                reservation.getVehicle().getVehicleType().name(),
                reservation.getSlot().getSlotId(),
                reservation.getStartTime().toString(),
                reservation.getEndTime().toString(),
                String.valueOf(reservation.isActive())
        );

        try {
            Files.writeString(filePath, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("Failed to save reservation {} to file", reservation.getReservationId(), e);
            throw new RuntimeException("Could not persist reservation", e);
        }
    }

    @Override
    public Optional<Reservation> findById(String reservationId) {
        return findAll().stream()
                .filter(r -> r.getReservationId().equals(reservationId))
                .findFirst();
    }

    @Override
    public List<Reservation> findAll() {
        List<Reservation> reservations = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return reservations;
        }

        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (line.isBlank()) continue;
                reservations.add(parseLine(line));
            }
        } catch (IOException e) {
            logger.error("Failed to read reservations from file", e);
            throw new RuntimeException("Could not read reservations", e);
        }

        return reservations;
    }

    private Reservation parseLine(String line) {
        String[] parts = line.split(DELIMITER);
        String reservationId = parts[0];
        String regNumber = parts[1];
        VehicleType vehicleType = VehicleType.valueOf(parts[2]);
        String slotId = parts[3];
        LocalDateTime startTime = LocalDateTime.parse(parts[4]);
        LocalDateTime endTime = LocalDateTime.parse(parts[5]);
        boolean active = Boolean.parseBoolean(parts[6]);

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