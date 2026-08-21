package com.gowshick.parking.model;

import java.time.LocalDateTime;

public class Reservation {

    private final String reservationId;
    private final Vehicle vehicle;
    private final ParkingSlot slot;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private boolean active;

    public Reservation(String reservationId, Vehicle vehicle, ParkingSlot slot,
                        LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Reservation start time must be before end time");
        }
        this.reservationId = reservationId;
        this.vehicle = vehicle;
        this.slot = slot;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = true;
    }

    public String getReservationId() {
        return reservationId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSlot getSlot() {
        return slot;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isGracePeriodExpired(LocalDateTime now) {
        return now.isAfter(startTime.plusMinutes(30));
    }

    @Override
    public String toString() {
        return "Reservation[" + reservationId + ", vehicle=" + vehicle +
               ", slot=" + slot.getSlotId() + ", start=" + startTime + ", active=" + active + "]";
    }
}