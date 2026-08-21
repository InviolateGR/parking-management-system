package com.gowshick.parking;

import com.gowshick.parking.enums.PaymentMethod;
import com.gowshick.parking.enums.SlotType;
import com.gowshick.parking.enums.VehicleType;
import com.gowshick.parking.factory.VehicleFactory;
import com.gowshick.parking.model.*;
import com.gowshick.parking.observer.AdminNotifier;
import com.gowshick.parking.observer.DisplayBoard;
import com.gowshick.parking.repository.BillRepository;
import com.gowshick.parking.repository.TicketRepository;
import com.gowshick.parking.repository.file.FileBillRepository;
import com.gowshick.parking.repository.file.FileTicketRepository;
import com.gowshick.parking.service.ParkingService;
import com.gowshick.parking.strategy.BikePricingStrategy;
import com.gowshick.parking.strategy.CarPricingStrategy;
import com.gowshick.parking.strategy.PricingStrategy;
import com.gowshick.parking.strategy.TruckPricingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        // 1. Set up the parking lot structure (2 floors, a few slots each)
        ParkingLot parkingLot = ParkingLot.getInstance();

        ParkingFloor floor1 = new ParkingFloor(1);
        floor1.addSlot(new ParkingSlot("F1-S1", SlotType.SMALL, 1));
        floor1.addSlot(new ParkingSlot("F1-S2", SlotType.MEDIUM, 1));
        floor1.addSlot(new ParkingSlot("F1-S3", SlotType.LARGE, 1));

        ParkingFloor floor2 = new ParkingFloor(2);
        floor2.addSlot(new ParkingSlot("F2-S1", SlotType.MEDIUM, 2));
        floor2.addSlot(new ParkingSlot("F2-S2", SlotType.MEDIUM, 2));

        // 2. Register observers on each floor
        floor1.registerObserver(new DisplayBoard());
        floor1.registerObserver(new AdminNotifier());
        floor2.registerObserver(new DisplayBoard());
        floor2.registerObserver(new AdminNotifier());

        parkingLot.addFloor(floor1);
        parkingLot.addFloor(floor2);

        // 3. Set up pricing strategies
        Map<VehicleType, PricingStrategy> pricingStrategies = new HashMap<>();
        pricingStrategies.put(VehicleType.BIKE, new BikePricingStrategy());
        pricingStrategies.put(VehicleType.CAR, new CarPricingStrategy());
        pricingStrategies.put(VehicleType.TRUCK, new TruckPricingStrategy());

        // 4. Set up repositories (file-based for now)
        TicketRepository ticketRepository = new FileTicketRepository("data/tickets.txt", parkingLot);
        BillRepository billRepository = new FileBillRepository("data/bills.txt", parkingLot);

        // 5. Construct the service — dependency injection by hand
        ParkingService parkingService = new ParkingService(
                parkingLot, ticketRepository, billRepository, pricingStrategies
        );

        logger.info("Parking Management System initialized with {} floors", parkingLot.getFloors().size());

        // 6. Quick end-to-end test: park a car, then exit it
        Vehicle car = VehicleFactory.createVehicle(VehicleType.CAR, "KA01AB1234");
        Ticket ticket = parkingService.parkVehicle(car);

        System.out.println("Issued ticket: " + ticket);

        Bill bill = parkingService.exitVehicle(ticket.getTicketId(), PaymentMethod.UPI);

        System.out.println("Generated bill: " + bill);
    }
}