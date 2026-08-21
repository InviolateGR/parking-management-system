package com.gowshick.parking.model;

import com.gowshick.parking.enums.SlotType;
import com.gowshick.parking.state.SlotState;
import com.gowshick.parking.state.FreeState;

public class ParkingSlot {

    private final String slotId;
    private final SlotType slotType;
    private final int floorNumber;

    private SlotState state;
    private Vehicle currentVehicle;

    public ParkingSlot(String slotId, SlotType slotType, int floorNumber) {
        this.slotId = slotId;
        this.slotType = slotType;
        this.floorNumber = floorNumber;
        this.state = new FreeState();
    }

    public String getSlotId() {
        return slotId;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    public void setCurrentVehicle(Vehicle vehicle) {
        this.currentVehicle = vehicle;
    }

    public SlotState getState() {
        return state;
    }

    public void setState(SlotState state) {
        this.state = state;
    }

    public boolean isCompatibleWith(com.gowshick.parking.enums.VehicleType vehicleType) {
        switch (vehicleType) {
            case BIKE:
                return true; // bikes can fit in any slot size
            case CAR:
                return slotType == SlotType.MEDIUM || slotType == SlotType.LARGE;
            case TRUCK:
                return slotType == SlotType.LARGE;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return "Slot[" + slotId + ", floor=" + floorNumber + ", type=" + slotType + ", state=" + state.getClass().getSimpleName() + "]";
    }
}