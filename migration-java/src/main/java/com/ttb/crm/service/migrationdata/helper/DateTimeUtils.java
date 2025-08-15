package com.ttb.crm.service.migrationdata.helper;

import com.ttb.crm.lib.crmssp_common_utils_lib.helper.Constant;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class DateTimeUtils {

    private DateTimeUtils() {
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(Constant.DATE_TIME_RESPONSE_FORMAT).withZone(ZoneId.of("UTC"));
    private static final ZonedDateTime FIXED_NOW = ZonedDateTime.now();

    public static ZonedDateTime getLocalDateTime() {
        return parseToZoneDateTime(FIXED_NOW.format(DATE_TIME_FORMATTER));
    }

    public static String getStaringLocalDateTime() {
        return FIXED_NOW.format(DATE_TIME_FORMATTER);
    }

    private static final List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS xxx"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS xxx"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.ISO_INSTANT
    );

    public static ZonedDateTime parseToZoneDateTime(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        for (DateTimeFormatter formatter : formatters) {
            try {
                return tryParsingWithFormatter(input, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Unrecognized datetime format: " + input);
    }

    public static ZonedDateTime tryParsingWithFormatter(String input, DateTimeFormatter formatter) {
        if (formatter.equals(DateTimeFormatter.ISO_INSTANT)) {
            Instant instant = Instant.parse(input);
            return instant.atZone(ZoneOffset.UTC);
        } else if (formatter.equals(formatters.getFirst())) {
            OffsetDateTime odt = OffsetDateTime.parse(input, formatter);
            return odt.atZoneSameInstant(ZoneOffset.UTC);
        } else {
            return tryParsingAsLocalDate(input, formatter);
        }
    }

    public static ZonedDateTime tryParsingAsLocalDate(String input, DateTimeFormatter formatter) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(input, formatter);
            return ldt.atZone(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            // Try parsing as LocalDate (e.g. "2025-06-05")
            LocalDate date = LocalDate.parse(input, formatter);
            return date.atStartOfDay(ZoneOffset.UTC);
        }
    }

    public static String convertToString(ZonedDateTime zonedDateTime) {
        return Optional.ofNullable(zonedDateTime)
                .map(dt -> dt.format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_RESPONSE_FORMAT)))
                .orElse(null);
    }
}

