package com.gowshick.parking.exception;

public class ReservationConflictException extends ParkingException {
    public ReservationConflictException(String message) {
        super(message);
    }
}