package com.ttb.crm.service.migrationdata.helper;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.ttb.crm.service.migrationdata.helper.DateTimeUtils.parseToZoneDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateTimeUtilsTest {

    @Test
    void testParseWithMillisecondsAndOffset() {
        ZonedDateTime result = parseToZoneDateTime("2025-08-12 14:30:15.123 +0700");
        assertEquals(2025, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(12, result.getDayOfMonth());
        assertEquals(ZoneOffset.UTC, result.getZone());
    }

    @Test
    void testParseWithNanosecondsAndXxxOffset() {
        ZonedDateTime result = parseToZoneDateTime("2025-08-12 14:30:15.1234567 +07:00");
        assertEquals(2025, result.getYear());
        assertEquals(ZoneOffset.UTC, result.getZone());
    }

    @Test
    void testParseWithoutMilliseconds() {
        ZonedDateTime result = parseToZoneDateTime("2025-08-12 14:30:15 +07:00");
        assertEquals(2025, result.getYear());
        assertEquals(ZoneOffset.UTC, result.getZone());
    }

    @Test
    void testParseDateOnly() {
        ZonedDateTime result = parseToZoneDateTime("2025-08-12");
        assertEquals(2025, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(12, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(ZoneOffset.UTC, result.getZone());
    }

    @Test
    void testParseIsoZonedDateTime() {
        ZonedDateTime result = parseToZoneDateTime("2025-08-12T14:30:15+07:00[Asia/Bangkok]");
        assertEquals(ZoneOffset.UTC, result.getZone());
    }

    @Test
    void testParseIsoInstant() {
        ZonedDateTime result = parseToZoneDateTime("2025-08-12T07:30:15Z");
        assertEquals(ZoneOffset.UTC, result.getZone());
    }

    @Test
    void testBlankInput() {
        assertNull(parseToZoneDateTime("   "));
    }

    @Test
    void testInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () ->
                parseToZoneDateTime("invalid-date"));
    }
}