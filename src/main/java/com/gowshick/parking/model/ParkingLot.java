package com.gowshick.parking.model;

import com.gowshick.parking.enums.VehicleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParkingLot {

    private static ParkingLot instance;

    private final List<ParkingFloor> floors;

    private ParkingLot() {
        this.floors = new ArrayList<>();
    }

    public static synchronized ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    public List<ParkingFloor> getFloors() {
        return floors;
    }

    public Optional<ParkingSlot> findAvailableSlot(VehicleType vehicleType) {
        for (ParkingFloor floor : floors) {
            Optional<ParkingSlot> slot = floor.findAvailableSlot(vehicleType);
            if (slot.isPresent()) {
                return slot;
            }
        }
        return Optional.empty();
    }

    public Optional<ParkingSlot> findSlotById(String slotId) {
        return floors.stream()
                .flatMap(floor -> floor.getSlots().stream())
                .filter(slot -> slot.getSlotId().equals(slotId))
                .findFirst();
    }
}