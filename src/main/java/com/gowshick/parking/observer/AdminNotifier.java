package com.gowshick.parking.observer;

import com.gowshick.parking.model.ParkingSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminNotifier implements ParkingObserver {

    private static final Logger logger = LoggerFactory.getLogger(AdminNotifier.class);

    @Override
    public void onSlotStatusChanged(ParkingSlot slot) {
        logger.info("[AdminNotifier] Alert: Slot {} on floor {} changed status to {}",
                slot.getSlotId(), slot.getFloorNumber(), slot.getState().getClass().getSimpleName());
    }
}