package com.gowshick.parking.state;

import com.gowshick.parking.model.ParkingSlot;
import com.gowshick.parking.model.Vehicle;
import com.gowshick.parking.model.Reservation;
import com.gowshick.parking.exception.SlotNotAvailableException;

public class FreeState implements SlotState {

    @Override
    public void park(ParkingSlot slot, Vehicle vehicle) {
        slot.setCurrentVehicle(vehicle);
        slot.setState(new OccupiedState());
    }

    @Override
    public void vacate(ParkingSlot slot) {
        throw new SlotNotAvailableException("Slot " + slot.getSlotId() + " is already free — cannot vacate.");
    }

    @Override
    public void reserve(ParkingSlot slot, Reservation reservation) {
        slot.setState(new ReservedState(reservation));
    }

    @Override
    public boolean isFree() {
        return true;
    }
}