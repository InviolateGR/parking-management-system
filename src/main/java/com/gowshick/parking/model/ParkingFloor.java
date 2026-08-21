package com.gowshick.parking.model;

import com.gowshick.parking.enums.VehicleType;
import com.gowshick.parking.observer.ParkingObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParkingFloor {

    private final int floorNumber;
    private final List<ParkingSlot> slots;
    private final List<ParkingObserver> observers;

    public ParkingFloor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.slots = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void addSlot(ParkingSlot slot) {
        slots.add(slot);
    }

    public List<ParkingSlot> getSlots() {
        return slots;
    }

    public void registerObserver(ParkingObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ParkingObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(ParkingSlot slot) {
        for (ParkingObserver observer : observers) {
            observer.onSlotStatusChanged(slot);
        }
    }

    public Optional<ParkingSlot> findAvailableSlot(VehicleType vehicleType) {
        return slots.stream()
                .filter(slot -> slot.isCompatibleWith(vehicleType))
                .filter(slot -> slot.getState().isFree())
                .findFirst();
    }

    public long countByStatus(String stateClassName) {
        return slots.stream()
                .filter(slot -> slot.getState().getClass().getSimpleName().equals(stateClassName))
                .count();
    }

    @Override
    public String toString() {
        return "Floor " + floorNumber + " [" + slots.size() + " slots]";
    }
}