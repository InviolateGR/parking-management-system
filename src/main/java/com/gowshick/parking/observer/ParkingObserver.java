package com.gowshick.parking.observer;

import com.gowshick.parking.model.ParkingSlot;

public interface ParkingObserver {
    void onSlotStatusChanged(ParkingSlot slot);
}