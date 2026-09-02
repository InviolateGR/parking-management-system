package com.gowshick.parking;

import com.gowshick.parking.enums.PaymentMethod;
import com.gowshick.parking.enums.SlotType;
import com.gowshick.parking.enums.VehicleType;
import com.gowshick.parking.exception.ParkingException;
import com.gowshick.parking.factory.VehicleFactory;
import com.gowshick.parking.model.*;
import com.gowshick.parking.observer.AdminNotifier;
import com.gowshick.parking.observer.DisplayBoard;
import com.gowshick.parking.repository.BillRepository;
import com.gowshick.parking.repository.ReservationRepository;
import com.gowshick.parking.repository.TicketRepository;
import com.gowshick.parking.repository.file.FileBillRepository;
import com.gowshick.parking.repository.file.FileReservationRepository;
import com.gowshick.parking.repository.file.FileTicketRepository;
import com.gowshick.parking.service.AnalyticsService;
import com.gowshick.parking.service.ParkingService;
import com.gowshick.parking.service.ReservationService;
import com.gowshick.parking.strategy.BikePricingStrategy;
import com.gowshick.parking.strategy.CarPricingStrategy;
import com.gowshick.parking.strategy.PricingStrategy;
import com.gowshick.parking.strategy.TruckPricingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        ParkingLot parkingLot = buildParkingLot();

        Map<VehicleType, PricingStrategy> pricingStrategies = buildPricingStrategies();

        TicketRepository ticketRepository = new FileTicketRepository("data/tickets.txt", parkingLot);
        BillRepository billRepository = new FileBillRepository("data/bills.txt", parkingLot);
        ReservationRepository reservationRepository = new FileReservationRepository("data/reservations.txt", parkingLot);

        ParkingService parkingService = new ParkingService(
                parkingLot, ticketRepository, billRepository, reservationRepository, pricingStrategies
        );
        ReservationService reservationService = new ReservationService(parkingLot, reservationRepository);
        AnalyticsService analyticsService = new AnalyticsService(ticketRepository, billRepository);

        logger.info("Parking Management System initialized with {} floors", parkingLot.getFloors().size());

        runMenu(parkingLot, parkingService, reservationService, analyticsService);
    }

    private static ParkingLot buildParkingLot() {
        ParkingLot parkingLot = ParkingLot.getInstance();

        ParkingFloor floor1 = new ParkingFloor(1);
        floor1.addSlot(new ParkingSlot("F1-S1", SlotType.SMALL, 1));
        floor1.addSlot(new ParkingSlot("F1-S2", SlotType.MEDIUM, 1));
        floor1.addSlot(new ParkingSlot("F1-S3", SlotType.LARGE, 1));

        ParkingFloor floor2 = new ParkingFloor(2);
        floor2.addSlot(new ParkingSlot("F2-S1", SlotType.MEDIUM, 2));
        floor2.addSlot(new ParkingSlot("F2-S2", SlotType.MEDIUM, 2));

        floor1.registerObserver(new DisplayBoard());
        floor1.registerObserver(new AdminNotifier());
        floor2.registerObserver(new DisplayBoard());
        floor2.registerObserver(new AdminNotifier());

        parkingLot.addFloor(floor1);
        parkingLot.addFloor(floor2);

        return parkingLot;
    }

    private static Map<VehicleType, PricingStrategy> buildPricingStrategies() {
        Map<VehicleType, PricingStrategy> pricingStrategies = new HashMap<>();
        pricingStrategies.put(VehicleType.BIKE, new BikePricingStrategy());
        pricingStrategies.put(VehicleType.CAR, new CarPricingStrategy());
        pricingStrategies.put(VehicleType.TRUCK, new TruckPricingStrategy());
        return pricingStrategies;
    }

    private static void runMenu(ParkingLot parkingLot, ParkingService parkingService,
                                 ReservationService reservationService, AnalyticsService analyticsService) {
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        handleParkVehicle(parkingService);
                        break;
                    case "2":
                        handleExitVehicle(parkingService);
                        break;
                    case "3":
                        handleReserveSlot(reservationService);
                        break;
                    case "4":
                        handleFloorStatus(parkingLot);
                        break;
                    case "5":
                        analyticsService.printSummary();
                        break;
                    case "6":
                        running = false;
                        System.out.println("Exiting. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please select a valid option.");
                }
            } catch (ParkingException e) {
                System.out.println("Operation failed: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n===== Parking Management System =====");
        System.out.println("1. Park a vehicle");
        System.out.println("2. Exit a vehicle");
        System.out.println("3. Reserve a slot");
        System.out.println("4. View floor status");
        System.out.println("5. View analytics summary");
        System.out.println("6. Quit");
        System.out.print("Choose an option: ");
    }

    private static void handleParkVehicle(ParkingService parkingService) {
        VehicleType type = promptVehicleType();
        System.out.print("Enter registration number: ");
        String regNumber = scanner.nextLine().trim();

        Vehicle vehicle = VehicleFactory.createVehicle(type, regNumber);
        Ticket ticket = parkingService.parkVehicle(vehicle);

        System.out.println("Vehicle parked successfully!");
        System.out.println(ticket);
    }

    private static void handleExitVehicle(ParkingService parkingService) {
        System.out.print("Enter ticket ID: ");
        String ticketId = scanner.nextLine().trim();

        PaymentMethod paymentMethod = promptPaymentMethod();

        Bill bill = parkingService.exitVehicle(ticketId, paymentMethod);

        System.out.println("Vehicle exited successfully!");
        System.out.println(bill);
    }

    private static void handleReserveSlot(ReservationService reservationService) {
        VehicleType type = promptVehicleType();
        System.out.print("Enter registration number: ");
        String regNumber = scanner.nextLine().trim();

        Vehicle vehicle = VehicleFactory.createVehicle(type, regNumber);

        System.out.print("Enter reservation duration in hours: ");
        int hours = Integer.parseInt(scanner.nextLine().trim());

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(hours);

        Reservation reservation = reservationService.createReservation(vehicle, type, startTime, endTime);

        System.out.println("Reservation created successfully!");
        System.out.println(reservation);
    }

    private static void handleFloorStatus(ParkingLot parkingLot) {
        for (ParkingFloor floor : parkingLot.getFloors()) {
            System.out.println("\nFloor " + floor.getFloorNumber() + ":");
            for (ParkingSlot slot : floor.getSlots()) {
                System.out.println("  " + slot);
            }
        }
    }

    private static VehicleType promptVehicleType() {
        System.out.print("Enter vehicle type (BIKE/CAR/TRUCK): ");
        String input = scanner.nextLine().trim().toUpperCase();
        return VehicleType.valueOf(input);
    }

    private static PaymentMethod promptPaymentMethod() {
        System.out.print("Enter payment method (CASH/CARD/UPI): ");
        String input = scanner.nextLine().trim().toUpperCase();
        return PaymentMethod.valueOf(input);
    }
}