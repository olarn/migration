package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.model.masterManagement.HolidayModel;
import com.ttb.crm.service.migrationdata.service.cache.SlaCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.ttb.crm.service.migrationdata.service.DatetimeService.addDaysSkippingWeekendsAndHolidays;
import static com.ttb.crm.service.migrationdata.service.DatetimeService.isHoliday;
import static com.ttb.crm.service.migrationdata.service.DatetimeService.isWeekend;
import static com.ttb.crm.service.migrationdata.service.DatetimeService.isWithinTimeRangeInclusive;
import static com.ttb.crm.service.migrationdata.service.DatetimeService.minusDaysSkippingWeekendsAndHolidays;
import static com.ttb.crm.service.migrationdata.service.DatetimeService.timeInRange;
import static com.ttb.crm.service.migrationdata.service.DatetimeService.toAsiaBangkokTimeZone;
import static com.ttb.crm.service.migrationdata.service.DatetimeService.toUTCZone;

@Service
@RequiredArgsConstructor
public class SlaService {
    private final SlaCache slaCache;
    final long millisecondsPerHour = 60L * 60 * 1000;
    final LocalTime startHourMorning = LocalTime.of(8, 0);
    final LocalTime endHourMorning = LocalTime.of(12, 0);
    final LocalTime startHourAfternoon = LocalTime.of(13, 0);
    final LocalTime endHourAfternoon = LocalTime.of(17, 0);
    final long morningWorkHours = Duration.between(startHourMorning, endHourMorning).toHours();
    final long afternoonWorkHours = Duration.between(startHourAfternoon, endHourAfternoon).toHours();
    final long workHours = morningWorkHours + afternoonWorkHours;

    //    @Cacheable(value = "slaStartDate", key = "#createDate.toEpochSecond()")
    public ZonedDateTime calculateSlaStartDate(ZonedDateTime createDate) {
        if (createDate == null) {
            throw new IllegalArgumentException("CreateDate must not be null");
        }

        List<HolidayModel> holidays = slaCache.getHolidays();

        ZonedDateTime startDate = toAsiaBangkokTimeZone(createDate);
        Predicate<LocalDate> isHoliday = isHoliday(holidays);

        ZonedDateTime slaStartDate = calculateSLAStartDateService(startDate).apply(isHoliday);
        return toUTCZone(slaStartDate);
    }

    //    @Cacheable(value = "slaCalculation", key = "#sla + '_' + #startDate.toEpochSecond()")
    public ZonedDateTime calculateSla(Float sla, ZonedDateTime startDate) {
        if (startDate == null ) {
            throw new IllegalArgumentException("StartDate must not be null");
        }

        if (sla == null ) {
            throw new IllegalArgumentException("SLA must not be null");
        }

        // Step 1: Prepare holiday and weekend logic
        List<HolidayModel> holidays = slaCache.getHolidays();
        Predicate<LocalDate> isHoliday = isHoliday(holidays);
        Function<ZonedDateTime, Function<Integer, ZonedDateTime>> plusDaysFunc = addDaysSkippingWeekendsAndHolidays(isHoliday).apply(DatetimeService::isWeekend);

        // Step 2: Convert start date to Asia/Bangkok timezone
        ZonedDateTime startDateInBangkok = toAsiaBangkokTimeZone(startDate);

        // Step 3: Calculate SLA start date
        ZonedDateTime slaStartDate = calculateSLAStartDateService(startDateInBangkok).apply(isHoliday);

        // Step 4: Convert SLA hours to duration in milliseconds
        double slaHours = sla;
        Duration slaDuration = Duration.ofMillis((long) (slaHours * workHours * millisecondsPerHour));

        // Step 5: Calculate SLA end date
        ZonedDateTime slaEndDate = calculateWorkHour(slaStartDate).apply(plusDaysFunc).apply(slaDuration);

        // Step 6: Convert to UTC
        return toUTCZone(slaEndDate);
    }

    public float calculateSpendingSla(ZonedDateTime startSlaDate, ZonedDateTime endSlaDate) {
        ZonedDateTime adjustedEndTime = endSlaDate.isBefore(startSlaDate) ? startSlaDate : endSlaDate;
        final List<HolidayModel> holidays = slaCache.getHolidays();

        // Convert dates to Asia/Bangkok timezone
        ZonedDateTime slaStartDate = toAsiaBangkokTimeZone(startSlaDate);
        ZonedDateTime now = toAsiaBangkokTimeZone(adjustedEndTime);

        // Calculate SLA start date
        ZonedDateTime startTime = calculateSLAStartDateService(slaStartDate)
                .apply(isHoliday(holidays));

        // Calculate SLA end time
        ZonedDateTime endTime = calculateSlaEndTimeProcess(now, minusDaysSkippingWeekendsAndHolidays(isHoliday(holidays))
                .apply(DatetimeService::isWeekend), isHoliday(holidays));

        // Ensure endTime is not before startTime
        if (startTime.isAfter(endTime)) {
            endTime = startTime;
        }

        // Calculate SLA spending
        return calculateSpendingSlaProcess(startTime, endTime, holidays);
    }

    public float calculateSpendingSlaProcess(ZonedDateTime startSlaDate, ZonedDateTime endTime, List<HolidayModel> holidays) {
        var calculateSpendingDurationFunc = calculateSpendingDuration(startHourMorning)
                .apply(endHourMorning)
                .apply(startHourAfternoon)
                .apply(endHourAfternoon);

        Duration monrningDuration = Duration.between(startHourMorning, endHourMorning);
        Duration afternoonDuration = Duration.between(startHourAfternoon, endHourAfternoon);
        var totalWorkHours = monrningDuration.plus(afternoonDuration);

        var day = ChronoUnit.DAYS.between(startSlaDate.toLocalDate(), endTime.toLocalDate());

        return Stream.iterate(startSlaDate, dt -> dt.plusDays(1))
                .limit(day + 1)
                .filter(dt -> !isWeekend(dt.toLocalDate()) && !isHoliday(holidays).test(dt.toLocalDate()))
                .map(dt -> {
                    if (dt.toLocalDate().isEqual(endTime.toLocalDate())) {
                        var startTime = day > 0 ? dt.with(startHourMorning)
                                : dt;
                        var duration = calculateSpendingDurationFunc
                                .apply(startTime.toLocalTime())
                                .apply(endTime.toLocalTime());
                        return computeWorkHourRatio(duration.toMillis(), totalWorkHours.toMillis());
                    }

                    var startTime = dt.toLocalDate().equals(startSlaDate.toLocalDate()) ? dt
                            : dt.with(startHourMorning);
                    var duration = calculateSpendingDurationFunc
                            .apply(startTime.toLocalTime())
                            .apply(endHourAfternoon);
                    return computeWorkHourRatio(duration.toMillis(), totalWorkHours.toMillis());
                })
                .reduce(Float::sum)
                .orElse(0f);
    }

    public ZonedDateTime calculateStartDateWithWorkHourRules(ZonedDateTime startDate, boolean isLunchBreakAndIsWorkday, boolean isWorkHoursAndNotWorkday) {
        if (isLunchBreakAndIsWorkday) {
            return startDate.with(startHourAfternoon);
        }
        if (isWorkHoursAndNotWorkday) {
            return startDate.with(startHourMorning);
        }
        return startDate;
    }

    public Function<Predicate<LocalDate>, ZonedDateTime> calculateSLAStartDateService(ZonedDateTime createDate) {

        return isHoliday -> Optional.of(createDate)
                .map(startDate -> {
                    final boolean beforeWorkHours = startDate.toLocalTime().isBefore(startHourMorning);
                    final boolean isAfterWorkHours = startDate.toLocalTime().isAfter(endHourAfternoon);
                    final boolean isWorkday = !isWeekend(startDate.toLocalDate()) && !isHoliday.test(startDate.toLocalDate());
                    final boolean isLunchBreak = timeInRange(endHourMorning).apply(startHourAfternoon).test(startDate.toLocalTime());

                    final boolean isLunchBreakAndIsWorkday = isLunchBreak && isWorkday;
                    final boolean isWorkHoursAndNotWorkday = beforeWorkHours || isAfterWorkHours || !isWorkday;
                    startDate = calculateStartDateWithWorkHourRules(startDate, isLunchBreakAndIsWorkday, isWorkHoursAndNotWorkday);

                    if (isAfterWorkHours || !isWorkday) {
                        startDate = addDaysSkippingWeekendsAndHolidays(isHoliday)
                                .apply(DatetimeService::isWeekend)
                                .apply(startDate)
                                .apply(1);
                    }
                    return startDate;
                })
                .orElseThrow();
    }

    public Function<Function<ZonedDateTime, Function<Integer, ZonedDateTime>>, Function<Duration, ZonedDateTime>> calculateWorkHour(ZonedDateTime startTime) {

        return plusDaysFunc -> duration -> {
            var plusDate = startTime.plus(duration);

            // lunch break
            if (startTime.isBefore(startTime.with(endHourMorning)) && plusDate.isAfter(startTime.with(endHourMorning))) {
                Duration exceedLunchBreak = Duration.between(startTime.with(endHourMorning), plusDate);
                return calculateWorkHour(startTime.with(startHourAfternoon)).apply(plusDaysFunc).apply(exceedLunchBreak);
            }

            // more than end afternoon work hours
            Duration exceedAfternoonWorkHours = Duration.between(startTime.with(endHourAfternoon), plusDate);
            if (exceedAfternoonWorkHours.isPositive()) {
                return calculateWorkHour(plusDaysFunc.apply(startTime.with(startHourMorning)).apply(1)).apply(plusDaysFunc).apply(exceedAfternoonWorkHours);
            }

            return plusDate;
        };
    }

    public Function<LocalTime, Function<LocalTime, Function<LocalTime, Function<LocalTime, Function<LocalTime, Duration>>>>> calculateSpendingDuration(LocalTime startMorning) {
        return endMorningTime -> startAfternoonTime -> endAfternoonTime -> start -> end ->
                this.durationBetween(start, end, startMorning, endMorningTime, startAfternoonTime, endAfternoonTime);
    }

    private Duration durationBetween(
            LocalTime start,
            LocalTime end,
            LocalTime startMorning,
            LocalTime endMorningTime,
            LocalTime startAfternoonTime,
            LocalTime endAfternoonTime) {
        final Duration breakDuration = Duration.between(endMorningTime, startAfternoonTime);

        var inMorningTime = isWithinTimeRangeInclusive(startMorning).apply(endMorningTime);
        var inAfternoonTime = isWithinTimeRangeInclusive(startAfternoonTime).apply(endAfternoonTime);
        var inLunchBreakTime = isWithinTimeRangeInclusive(endMorningTime).apply(startAfternoonTime);

        if (inMorningTime.test(start) && inAfternoonTime.test(end)) {
            return Duration.between(start, end).minus(breakDuration);
        }
        if (inMorningTime.test(start) && inLunchBreakTime.test(end)) {
            return Duration.between(start, endMorningTime);
        }
        if (inLunchBreakTime.test(start) && inAfternoonTime.test(end)) {
            return Duration.between(startAfternoonTime, end);
        }
        return Duration.between(start, end);
    }

    private float computeWorkHourRatio(long spendingWorkHours, long totalWorkHours) {
        return ((float) spendingWorkHours / totalWorkHours);
    }

    public ZonedDateTime calculateSlaEndTimeProcess(
            ZonedDateTime endTime,
            Function<ZonedDateTime, Function<Integer, ZonedDateTime>> minusDays,
            Predicate<LocalDate> isHoliday) {

        Predicate<ZonedDateTime> isBeforeWorkHours = dt -> dt.toLocalTime().isBefore(startHourMorning);
        Predicate<ZonedDateTime> isWeekendOrHoliday = dt -> isWeekend(dt.toLocalDate()) || isHoliday.test(dt.toLocalDate());
        Predicate<ZonedDateTime> isInLunchTime = dt -> isWithinTimeRangeInclusive(endHourMorning).apply(startHourAfternoon).test(dt.toLocalTime());
        Predicate<ZonedDateTime> isAfterWorkHours = dt -> dt.toLocalTime().isAfter(endHourAfternoon);

        return Stream.<Map.Entry<Predicate<ZonedDateTime>, Function<ZonedDateTime, ZonedDateTime>>>of(
                        Map.entry(isBeforeWorkHours.or(isWeekendOrHoliday),
                                (ZonedDateTime dt) ->
                                        minusDays.apply(dt).apply(1).with(endHourAfternoon)
                        ),
                        Map.entry(isInLunchTime, (ZonedDateTime dt) -> dt.with(endHourMorning)),
                        Map.entry(isAfterWorkHours, (ZonedDateTime dt) -> dt.with(endHourAfternoon))
                )
                .filter(entry -> entry.getKey().test(endTime))
                .map(entry -> entry.getValue().apply(endTime))
                .findFirst()
                .orElse(endTime);
    }
}