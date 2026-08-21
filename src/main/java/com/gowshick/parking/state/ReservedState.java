package com.gowshick.parking.state;

import com.gowshick.parking.model.ParkingSlot;
import com.gowshick.parking.model.Vehicle;
import com.gowshick.parking.model.Reservation;
import com.gowshick.parking.exception.SlotNotAvailableException;

public class ReservedState implements SlotState {

    private final Reservation reservation;

    public ReservedState(Reservation reservation) {
        this.reservation = reservation;
    }

    public Reservation getReservation() {
        return reservation;
    }

    @Override
    public void park(ParkingSlot slot, Vehicle vehicle) {
        if (!reservation.getVehicle().equals(vehicle)) {
            throw new SlotNotAvailableException(
                "Slot " + slot.getSlotId() + " is reserved for another vehicle."
            );
        }
        slot.setCurrentVehicle(vehicle);
        slot.setState(new OccupiedState());
    }

    @Override
    public void vacate(ParkingSlot slot) {
        throw new SlotNotAvailableException("Slot " + slot.getSlotId() + " is reserved, not occupied — nothing to vacate.");
    }

    @Override
    public void reserve(ParkingSlot slot, Reservation reservation) {
        throw new SlotNotAvailableException("Slot " + slot.getSlotId() + " is already reserved.");
    }

    @Override
    public boolean isFree() {
        return false;
    }
}