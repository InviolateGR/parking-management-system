package com.gowshick.parking.state;

import com.gowshick.parking.model.ParkingSlot;
import com.gowshick.parking.model.Vehicle;
import com.gowshick.parking.model.Reservation;

public interface SlotState {
    void park(ParkingSlot slot, Vehicle vehicle);
    void vacate(ParkingSlot slot);
    void reserve(ParkingSlot slot, Reservation reservation);
    boolean isFree();
}