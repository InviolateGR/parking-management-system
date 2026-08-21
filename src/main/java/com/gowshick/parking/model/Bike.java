package com.gowshick.parking.model;

import com.gowshick.parking.enums.VehicleType;

public class Bike extends Vehicle {
    public Bike(String registrationNumber) {
        super(registrationNumber, VehicleType.BIKE);
    }
}