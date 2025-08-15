package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.constants.CommonConstants;
import com.ttb.crm.service.migrationdata.model.masterManagement.HolidayModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DatetimeService {

    public static Function<LocalTime, Predicate<LocalTime>> isWithinTimeRangeInclusive(LocalTime startTime) {
        return endTime -> currentTime ->
                !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
    }

    public static Function<LocalTime, Predicate<LocalTime>> timeInRange(LocalTime startTime) {
        return endTime -> currentTime ->
                !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
    }

    public static Predicate<LocalDate> isHoliday(List<HolidayModel> holidays) {
        return date -> holidays.stream().anyMatch(holiday -> holiday.getHolidayDate().equals(date));
    }

    public static boolean isWeekend(LocalDate dateTime) {
        return dateTime.getDayOfWeek().getValue() == 6 || dateTime.getDayOfWeek().getValue() == 7;
    }

    public static Function<Predicate<LocalDate>, Function<ZonedDateTime, Function<Integer, ZonedDateTime>>> addDaysSkippingWeekendsAndHolidays(Predicate<LocalDate> isHoliday) {
        return isWeekend -> dateTime -> days ->
                Stream.iterate(dateTime.plusDays(1), d -> d.plusDays(1))
                        .filter(d -> !isWeekend.test(d.toLocalDate()) && !isHoliday.test(d.toLocalDate()))
                        .limit(days)
                        .reduce((first, second) -> second)
                        .orElse(dateTime);
    }

    public static Function<Predicate<LocalDate>, Function<ZonedDateTime, Function<Integer, ZonedDateTime>>> minusDaysSkippingWeekendsAndHolidays(Predicate<LocalDate> isHoliday) {
        return isWeekend -> dateTime -> days ->
                Stream.iterate(dateTime.minusDays(1), d -> d.minusDays(1))
                        .filter(d ->
                                !isWeekend.test(d.toLocalDate()) &&
                                        !isHoliday.test(d.toLocalDate())
                        )
                        .limit(days)
                        .reduce((first, second) -> second)
                        .orElse(dateTime);
    }

    public static ZonedDateTime toAsiaBangkokTimeZone(ZonedDateTime dateTime) {
        return dateTime.withZoneSameInstant(CommonConstants.asiaBangkokZoneId);
    }

    public static ZonedDateTime toUTCZone(ZonedDateTime dateTime) {
        return dateTime.withZoneSameInstant(CommonConstants.utcZoneId);
    }
}
