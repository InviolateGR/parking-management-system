package com.gowshick.parking.repository.file;

import com.gowshick.parking.factory.VehicleFactory;
import com.gowshick.parking.enums.VehicleType;
import com.gowshick.parking.model.ParkingLot;
import com.gowshick.parking.model.ParkingSlot;
import com.gowshick.parking.model.Ticket;
import com.gowshick.parking.model.Vehicle;
import com.gowshick.parking.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileTicketRepository implements TicketRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileTicketRepository.class);
    private static final String DELIMITER = "\\|";

    private final Path filePath;
    private final ParkingLot parkingLot;

    public FileTicketRepository(String filePath, ParkingLot parkingLot) {
        this.filePath = Paths.get(filePath);
        this.parkingLot = parkingLot;
    }

    @Override
    public void save(Ticket ticket) {
        String line = String.join("|",
                ticket.getTicketId(),
                ticket.getVehicle().getRegistrationNumber(),
                ticket.getVehicle().getVehicleType().name(),
                ticket.getSlot().getSlotId(),
                ticket.getEntryTime().toString()
        );

        try {
            Files.writeString(filePath, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("Failed to save ticket {} to file", ticket.getTicketId(), e);
            throw new RuntimeException("Could not persist ticket", e);
        }
    }

    @Override
    public Optional<Ticket> findById(String ticketId) {
        return findAll().stream()
                .filter(t -> t.getTicketId().equals(ticketId))
                .findFirst();
    }

    @Override
    public List<Ticket> findAll() {
        List<Ticket> tickets = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return tickets;
        }

        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (line.isBlank()) continue;
                tickets.add(parseLine(line));
            }
        } catch (IOException e) {
            logger.error("Failed to read tickets from file", e);
            throw new RuntimeException("Could not read tickets", e);
        }

        return tickets;
    }

    private Ticket parseLine(String line) {
        String[] parts = line.split(DELIMITER);
        String ticketId = parts[0];
        String regNumber = parts[1];
        VehicleType vehicleType = VehicleType.valueOf(parts[2]);
        String slotId = parts[3];
        LocalDateTime entryTime = LocalDateTime.parse(parts[4]);

        Vehicle vehicle = VehicleFactory.createVehicle(vehicleType, regNumber);

        ParkingSlot slot = parkingLot.findSlotById(slotId)
                .orElseThrow(() -> new IllegalStateException(
                        "Referenced slot " + slotId + " not found in parking lot"));

        return new Ticket(ticketId, vehicle, slot, entryTime);
    }
}