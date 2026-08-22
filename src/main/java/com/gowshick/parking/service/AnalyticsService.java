package com.gowshick.parking.service;

import com.gowshick.parking.model.Bill;
import com.gowshick.parking.model.Ticket;
import com.gowshick.parking.repository.BillRepository;
import com.gowshick.parking.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private final TicketRepository ticketRepository;
    private final BillRepository billRepository;

    public AnalyticsService(TicketRepository ticketRepository, BillRepository billRepository) {
        this.ticketRepository = ticketRepository;
        this.billRepository = billRepository;
    }

    public long getTotalVehiclesParked() {
        return ticketRepository.findAll().size();
    }

    public double getTotalRevenue() {
        return billRepository.findAll().stream()
                .mapToDouble(Bill::getAmount)
                .sum();
    }

    public double getAverageParkingDurationMinutes() {
        List<Bill> bills = billRepository.findAll();

        if (bills.isEmpty()) {
            return 0.0;
        }

        return bills.stream()
                .mapToLong(bill -> Duration.between(
                        bill.getTicket().getEntryTime(),
                        bill.getExitTime()
                ).toMinutes())
                .average()
                .orElse(0.0);
    }

    public Map<Integer, Long> getVehicleCountByFloor() {
        return ticketRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        ticket -> ticket.getSlot().getFloorNumber(),
                        Collectors.counting()
                ));
    }

    public int getPeakFloor() {
        Map<Integer, Long> countByFloor = getVehicleCountByFloor();

        return countByFloor.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(-1);
    }

    public void printSummary() {
        logger.info("=== Analytics Summary ===");
        logger.info("Total vehicles parked: {}", getTotalVehiclesParked());
        logger.info("Total revenue: {}", getTotalRevenue());
        logger.info("Average parking duration (minutes): {}", getAverageParkingDurationMinutes());
        logger.info("Vehicle count by floor: {}", getVehicleCountByFloor());
        logger.info("Peak floor: {}", getPeakFloor());
    }
}