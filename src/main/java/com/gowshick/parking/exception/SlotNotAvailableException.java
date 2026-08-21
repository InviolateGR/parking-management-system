package com.gowshick.parking.exception;

public class SlotNotAvailableException extends ParkingException {
    public SlotNotAvailableException(String message) {
        super(message);
    }
}