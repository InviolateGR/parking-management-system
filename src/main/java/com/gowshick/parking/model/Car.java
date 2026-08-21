package com.gowshick.parking.model;

import com.gowshick.parking.enums.VehicleType;

public class Car extends Vehicle {
    public Car(String registrationNumber) {
        super(registrationNumber, VehicleType.CAR);
    }
}