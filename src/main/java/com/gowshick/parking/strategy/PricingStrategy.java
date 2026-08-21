package com.gowshick.parking.strategy;

import java.time.Duration;

public interface PricingStrategy {
    double calculateFare(Duration parkedDuration);
}