package com.gowshick.parking.exception;

public class InvalidTicketException extends ParkingException {
    public InvalidTicketException(String message) {
        super(message);
    }
}