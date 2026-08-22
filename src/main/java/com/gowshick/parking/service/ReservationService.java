package com.gowshick.parking.service;

import com.gowshick.parking.enums.VehicleType;
import com.gowshick.parking.exception.ReservationConflictException;
import com.gowshick.parking.model.ParkingFloor;
import com.gowshick.parking.model.ParkingLot;
import com.gowshick.parking.model.ParkingSlot;
import com.gowshick.parking.model.Reservation;
import com.gowshick.parking.model.Vehicle;
import com.gowshick.parking.repository.ReservationRepository;
import com.gowshick.parking.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    private final ParkingLot parkingLot;
    private final ReservationRepository reservationRepository;

    public ReservationService(ParkingLot parkingLot, ReservationRepository reservationRepository) {
        this.parkingLot = parkingLot;
        this.reservationRepository = reservationRepository;
    }

    public Reservation createReservation(Vehicle vehicle, VehicleType vehicleType,
                                          LocalDateTime startTime, LocalDateTime endTime) {

        Optional<ParkingSlot> slotOpt = parkingLot.findAvailableSlot(vehicleType);

        if (slotOpt.isEmpty()) {
            logger.warn("Reservation denied — no available slot for vehicle {}", vehicle);
            throw new ReservationConflictException(
                    "No available slot to reserve for vehicle type: " + vehicleType);
        }

        ParkingSlot slot = slotOpt.get();

        Reservation reservation = new Reservation(
                IdGenerator.generate("RES"),
                vehicle,
                slot,
                startTime,
                endTime
        );

        slot.getState().reserve(slot, reservation);
        notifyFloorObservers(slot);

        reservationRepository.save(reservation);
        logger.info("Reservation created successfully: {}", reservation);

        return reservation;
    }

    public void releaseExpiredReservation(Reservation reservation) {
        if (!reservation.isActive()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (reservation.isGracePeriodExpired(now)) {
            ParkingSlot slot = reservation.getSlot();
            reservation.deactivate();
            slot.getState().release(slot);
            notifyFloorObservers(slot);
            logger.info("Reservation {} expired — slot {} released back to free",
                    reservation.getReservationId(), slot.getSlotId());
        }
    }

    private void notifyFloorObservers(ParkingSlot slot) {
        ParkingFloor floor = parkingLot.getFloors().stream()
                .filter(f -> f.getFloorNumber() == slot.getFloorNumber())
                .findFirst()
                .orElse(null);

        if (floor != null) {
            floor.notifyObservers(slot);
        }
    }
}