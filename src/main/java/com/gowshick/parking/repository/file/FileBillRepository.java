package com.gowshick.parking.repository.file;

import com.gowshick.parking.enums.PaymentMethod;
import com.gowshick.parking.enums.VehicleType;
import com.gowshick.parking.factory.VehicleFactory;
import com.gowshick.parking.model.*;
import com.gowshick.parking.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileBillRepository implements BillRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileBillRepository.class);
    private static final String DELIMITER = "\\|";

    private final Path filePath;
    private final ParkingLot parkingLot;

    public FileBillRepository(String filePath, ParkingLot parkingLot) {
        this.filePath = Paths.get(filePath);
        this.parkingLot = parkingLot;
    }

    @Override
    public void save(Bill bill) {
        Ticket ticket = bill.getTicket();
        String line = String.join("|",
                bill.getBillId(),
                ticket.getTicketId(),
                ticket.getVehicle().getRegistrationNumber(),
                ticket.getVehicle().getVehicleType().name(),
                ticket.getSlot().getSlotId(),
                ticket.getEntryTime().toString(),
                bill.getExitTime().toString(),
                String.valueOf(bill.getAmount()),
                bill.getPaymentMethod().name()
        );

        try {
            Files.writeString(filePath, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("Failed to save bill {} to file", bill.getBillId(), e);
            throw new RuntimeException("Could not persist bill", e);
        }
    }

    @Override
    public Optional<Bill> findById(String billId) {
        return findAll().stream()
                .filter(b -> b.getBillId().equals(billId))
                .findFirst();
    }

    @Override
    public List<Bill> findAll() {
        List<Bill> bills = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return bills;
        }

        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (line.isBlank()) continue;
                bills.add(parseLine(line));
            }
        } catch (IOException e) {
            logger.error("Failed to read bills from file", e);
            throw new RuntimeException("Could not read bills", e);
        }

        return bills;
    }

    private Bill parseLine(String line) {
        String[] parts = line.split(DELIMITER);
        String billId = parts[0];
        String ticketId = parts[1];
        String regNumber = parts[2];
        VehicleType vehicleType = VehicleType.valueOf(parts[3]);
        String slotId = parts[4];
        LocalDateTime entryTime = LocalDateTime.parse(parts[5]);
        LocalDateTime exitTime = LocalDateTime.parse(parts[6]);
        double amount = Double.parseDouble(parts[7]);
        PaymentMethod paymentMethod = PaymentMethod.valueOf(parts[8]);

        Vehicle vehicle = VehicleFactory.createVehicle(vehicleType, regNumber);
        ParkingSlot slot = parkingLot.findSlotById(slotId)
                .orElseThrow(() -> new IllegalStateException(
                        "Referenced slot " + slotId + " not found in parking lot"));

        Ticket ticket = new Ticket(ticketId, vehicle, slot, entryTime);
        return new Bill(billId, ticket, exitTime, amount, paymentMethod);
    }
}