package edu.orders.server.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class BaseDateService {

    @Value("${data.id.pattern}")
    private String pattern;

    @Value("${data.id.zone.id}")
    private String zoneId;

    public String getBaseTimestampId() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(getBaseFormatter()) + getRandomSuffix();
    }

    private DateTimeFormatter getBaseFormatter() {
        return DateTimeFormatter.ofPattern(pattern).withZone(getZoneIdi());
    }

    private ZoneId getZoneIdi() {
        return ZoneId.of(zoneId);
    }

    private static int getRandomSuffix() {
        final int MIN_ID = 1000;
        final int MAX_ID = 9999;
        final int RANGE = MAX_ID - MIN_ID + 1;
        return new Random().nextInt(RANGE) + MIN_ID;
    }
}
