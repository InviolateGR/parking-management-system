package com.gowshick.parking.util;

import java.util.UUID;

public class IdGenerator {

    private IdGenerator() {
        // prevent instantiation — this is a pure utility class
    }

    public static String generate(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}