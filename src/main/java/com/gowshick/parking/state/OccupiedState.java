package com.gowshick.parking.state;

import com.gowshick.parking.model.ParkingSlot;
import com.gowshick.parking.model.Vehicle;
import com.gowshick.parking.model.Reservation;
import com.gowshick.parking.exception.SlotNotAvailableException;

public class OccupiedState implements SlotState {

    @Override
    public void park(ParkingSlot slot, Vehicle vehicle) {
        throw new SlotNotAvailableException("Slot " + slot.getSlotId() + " is already occupied.");
    }

    @Override
    public void vacate(ParkingSlot slot) {
        slot.setCurrentVehicle(null);
        slot.setState(new FreeState());
    }

    @Override
    public void reserve(ParkingSlot slot, Reservation reservation) {
        throw new SlotNotAvailableException("Slot " + slot.getSlotId() + " is currently occupied — cannot reserve.");
    }

    @Override
    public boolean isFree() {
        return false;
    }

    @Override
    public void release(ParkingSlot slot) {
        throw new SlotNotAvailableException("Slot " + slot.getSlotId() + " is occupied, not reserved — cannot release a reservation.");
    }
}