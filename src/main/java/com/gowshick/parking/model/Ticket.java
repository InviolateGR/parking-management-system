package com.gowshick.parking.model;

import java.time.LocalDateTime;

public class Ticket {

    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSlot slot;
    private final LocalDateTime entryTime;

    public Ticket(String ticketId, Vehicle vehicle, ParkingSlot slot, LocalDateTime entryTime) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.slot = slot;
        this.entryTime = entryTime;
    }

    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSlot getSlot() {
        return slot;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    @Override
    public String toString() {
        return "Ticket[" + ticketId + ", vehicle=" + vehicle + ", slot=" + slot.getSlotId() +
               ", entry=" + entryTime + "]";
    }
}