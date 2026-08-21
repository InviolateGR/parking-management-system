package com.gowshick.parking.strategy;

import java.time.Duration;

public class BikePricingStrategy implements PricingStrategy {

    private static final double FIRST_HOUR_RATE = 20.0;
    private static final double ADDITIONAL_HOUR_RATE = 10.0;

    @Override
    public double calculateFare(Duration parkedDuration) {
        long totalMinutes = parkedDuration.toMinutes();
        long totalHours = (long) Math.ceil(totalMinutes / 60.0);

        if (totalHours <= 0) {
            totalHours = 1;
        }

        if (totalHours == 1) {
            return FIRST_HOUR_RATE;
        }

        return FIRST_HOUR_RATE + (totalHours - 1) * ADDITIONAL_HOUR_RATE;
    }
}