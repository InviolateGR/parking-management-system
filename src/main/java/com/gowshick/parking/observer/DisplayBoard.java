package com.gowshick.parking.observer;

import com.gowshick.parking.model.ParkingSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisplayBoard implements ParkingObserver {

    private static final Logger logger = LoggerFactory.getLogger(DisplayBoard.class);

    @Override
    public void onSlotStatusChanged(ParkingSlot slot) {
        logger.info("[DisplayBoard] Slot {} on floor {} is now {}",
                slot.getSlotId(), slot.getFloorNumber(), slot.getState().getClass().getSimpleName());
    }
}