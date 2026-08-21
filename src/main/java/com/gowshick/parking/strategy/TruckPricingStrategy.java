package com.gowshick.parking.strategy;

import java.time.Duration;

public class TruckPricingStrategy implements PricingStrategy {

    private static final double HOURLY_RATE = 100.0;
    private static final double DAILY_CAP = 1500.0;

    @Override
    public double calculateFare(Duration parkedDuration) {
        long totalMinutes = parkedDuration.toMinutes();
        long totalHours = (long) Math.ceil(totalMinutes / 60.0);

        if (totalHours <= 0) {
            totalHours = 1;
        }

        double fare = totalHours * HOURLY_RATE;
        return Math.min(fare, DAILY_CAP);
    }
}