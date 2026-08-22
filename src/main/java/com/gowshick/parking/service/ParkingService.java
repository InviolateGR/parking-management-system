package com.gowshick.parking.service;

import com.gowshick.parking.enums.VehicleType;
import com.gowshick.parking.exception.InvalidTicketException;
import com.gowshick.parking.exception.SlotNotAvailableException;
import com.gowshick.parking.model.*;
import com.gowshick.parking.repository.BillRepository;
import com.gowshick.parking.repository.ReservationRepository;
import com.gowshick.parking.repository.TicketRepository;
import com.gowshick.parking.strategy.PricingStrategy;
import com.gowshick.parking.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ParkingService {

    private static final Logger logger = LoggerFactory.getLogger(ParkingService.class);

    private final ParkingLot parkingLot;
    private final TicketRepository ticketRepository;
    private final BillRepository billRepository;
    private final ReservationRepository reservationRepository;
    private final Map<VehicleType, PricingStrategy> pricingStrategies;

    public ParkingService(ParkingLot parkingLot,
                           TicketRepository ticketRepository,
                           BillRepository billRepository,
                           ReservationRepository reservationRepository,
                           Map<VehicleType, PricingStrategy> pricingStrategies) {
        this.parkingLot = parkingLot;
        this.ticketRepository = ticketRepository;
        this.billRepository = billRepository;
        this.reservationRepository = reservationRepository;
        this.pricingStrategies = pricingStrategies;
    }

    public Ticket parkVehicle(Vehicle vehicle) {
        ParkingSlot slot = findReservedSlotFor(vehicle)
                .orElseGet(() -> findWalkInSlotFor(vehicle));

        slot.getState().park(slot, vehicle);

        ParkingFloor floor = findFloorForSlot(slot);
        if (floor != null) {
            floor.notifyObservers(slot);
        }

        Ticket ticket = new Ticket(
                IdGenerator.generate("TICKET"),
                vehicle,
                slot,
                LocalDateTime.now()
        );

        ticketRepository.save(ticket);
        logger.info("Vehicle parked successfully: {}", ticket);

        return ticket;
    }

    private Optional<ParkingSlot> findReservedSlotFor(Vehicle vehicle) {
        List<Reservation> reservations = reservationRepository.findAll();

        return reservations.stream()
                .filter(Reservation::isActive)
                .filter(r -> r.getVehicle().equals(vehicle))
                .filter(r -> !r.isGracePeriodExpired(LocalDateTime.now()))
                .map(Reservation::getSlot)
                .findFirst();
    }

    private ParkingSlot findWalkInSlotFor(Vehicle vehicle) {
        return parkingLot.findAvailableSlot(vehicle.getVehicleType())
                .orElseThrow(() -> {
                    logger.warn("Parking denied — no available slot for vehicle {}", vehicle);
                    return new SlotNotAvailableException(
                            "No available slot for vehicle type: " + vehicle.getVehicleType());
                });
    }

    public Bill exitVehicle(String ticketId, com.gowshick.parking.enums.PaymentMethod paymentMethod) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    logger.error("Exit failed — invalid ticket ID: {}", ticketId);
                    return new InvalidTicketException("No such ticket: " + ticketId);
                });

        ParkingSlot slot = ticket.getSlot();
        slot.getState().vacate(slot);

        ParkingFloor floor = findFloorForSlot(slot);
        if (floor != null) {
            floor.notifyObservers(slot);
        }

        LocalDateTime exitTime = LocalDateTime.now();
        Duration parkedDuration = Duration.between(ticket.getEntryTime(), exitTime);

        PricingStrategy strategy = pricingStrategies.get(ticket.getVehicle().getVehicleType());
        double fare = strategy.calculateFare(parkedDuration);

        Bill bill = new Bill(
                IdGenerator.generate("BILL"),
                ticket,
                exitTime,
                fare,
                paymentMethod
        );

        billRepository.save(bill);
        logger.info("Vehicle exited successfully: {}", bill);

        return bill;
    }

    private ParkingFloor findFloorForSlot(ParkingSlot slot) {
        return parkingLot.getFloors().stream()
                .filter(floor -> floor.getFloorNumber() == slot.getFloorNumber())
                .findFirst()
                .orElse(null);
    }
}