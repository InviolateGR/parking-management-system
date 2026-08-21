package com.gowshick.parking.model;

import com.gowshick.parking.enums.VehicleType;

public class Truck extends Vehicle {
    public Truck(String registrationNumber) {
        super(registrationNumber, VehicleType.TRUCK);
    }
}