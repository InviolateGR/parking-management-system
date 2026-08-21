package com.gowshick.parking.factory;

import com.gowshick.parking.enums.VehicleType;
import com.gowshick.parking.model.Bike;
import com.gowshick.parking.model.Car;
import com.gowshick.parking.model.Truck;
import com.gowshick.parking.model.Vehicle;

public class VehicleFactory {

    public static Vehicle createVehicle(VehicleType type, String registrationNumber) {
        switch (type) {
            case BIKE:
                return new Bike(registrationNumber);
            case CAR:
                return new Car(registrationNumber);
            case TRUCK:
                return new Truck(registrationNumber);
            default:
                throw new IllegalArgumentException("Unsupported vehicle type: " + type);
        }
    }
}