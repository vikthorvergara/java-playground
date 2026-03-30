package com.github.vikthorvergara.jdk;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

public class DateTimeDurationPOC {

    public static void main(String[] args) {
        durationBetweenInstants();
        durationBetweenTimes();
        durationParsing();
        periodBetweenDates();
        periodParsing();
        chronoUnitCalculations();
        builtInTemporalAdjusters();
        customTemporalAdjusters();
    }

    static void durationBetweenInstants() {
        System.out.println("=== Duration Between Instants ===");

        var start = Instant.parse("2025-03-15T08:00:00Z");
        var end = Instant.parse("2025-03-17T14:30:45Z");
        var duration = Duration.between(start, end);

        System.out.println("Start: " + start);
        System.out.println("End: " + end);
        System.out.println("Duration: " + duration);
        System.out.println("Total seconds: " + duration.toSeconds());
        System.out.println("Total minutes: " + duration.toMinutes());
        System.out.println("Total hours: " + duration.toHours());
        System.out.println("Days part: " + duration.toDaysPart());
        System.out.println("Hours part: " + duration.toHoursPart());
        System.out.println("Minutes part: " + duration.toMinutesPart());
        System.out.println("Seconds part: " + duration.toSecondsPart());

        var doubled = duration.multipliedBy(2);
        var halved = duration.dividedBy(2);
        System.out.println("Doubled: " + doubled);
        System.out.println("Halved: " + halved);
        System.out.println("Negated: " + duration.negated());
        System.out.println("Absolute: " + duration.negated().abs());
        System.out.println();
    }

    static void durationBetweenTimes() {
        System.out.println("=== Duration Between LocalTimes ===");

        var morningShiftStart = LocalTime.of(6, 0);
        var morningShiftEnd = LocalTime.of(14, 30);
        var shiftDuration = Duration.between(morningShiftStart, morningShiftEnd);

        System.out.println("Shift: " + morningShiftStart + " to " + morningShiftEnd);
        System.out.println("Duration: " + shiftDuration);
        System.out.println("Hours: " + shiftDuration.toHours() + "h " + shiftDuration.toMinutesPart() + "m");

        var lunchBreak = Duration.ofMinutes(60);
        var effectiveWork = shiftDuration.minus(lunchBreak);
        System.out.println("Lunch break: " + lunchBreak);
        System.out.println("Effective work: " + effectiveWork);

        var overtime = Duration.ofHours(2).plusMinutes(15);
        var totalWork = effectiveWork.plus(overtime);
        System.out.println("Overtime: " + overtime);
        System.out.println("Total work: " + totalWork);

        System.out.println("Is negative: " + shiftDuration.isNegative());
        System.out.println("Is zero: " + Duration.ZERO.isZero());
        System.out.println();
    }

    static void durationParsing() {
        System.out.println("=== Duration ISO-8601 Parsing ===");

        var twoHoursThirty = Duration.parse("PT2H30M");
        var oneDay = Duration.parse("PT24H");
        var complexDuration = Duration.parse("PT1H15M30.5S");
        var negativeDuration = Duration.parse("PT-6H3M");

        System.out.println("PT2H30M -> " + twoHoursThirty + " (" + twoHoursThirty.toMinutes() + " minutes)");
        System.out.println("PT24H -> " + oneDay + " (" + oneDay.toHours() + " hours)");
        System.out.println("PT1H15M30.5S -> " + complexDuration);
        System.out.println("PT-6H3M -> " + negativeDuration);

        var fromHours = Duration.ofHours(3);
        var fromMinutes = Duration.ofMinutes(90);
        var fromSeconds = Duration.ofSeconds(3661);
        var fromMillis = Duration.ofMillis(1500);

        System.out.println("3 hours: " + fromHours);
        System.out.println("90 minutes: " + fromMinutes);
        System.out.println("3661 seconds: " + fromSeconds + " -> " + fromSeconds.toHoursPart() + "h " + fromSeconds.toMinutesPart() + "m " + fromSeconds.toSecondsPart() + "s");
        System.out.println("1500 millis: " + fromMillis);
        System.out.println();
    }

    static void periodBetweenDates() {
        System.out.println("=== Period Between Dates ===");

        var projectStart = LocalDate.of(2024, Month.JANUARY, 15);
        var projectEnd = LocalDate.of(2025, Month.SEPTEMBER, 30);
        var period = Period.between(projectStart, projectEnd);

        System.out.println("Project start: " + projectStart);
        System.out.println("Project end: " + projectEnd);
        System.out.println("Period: " + period);
        System.out.println("Years: " + period.getYears());
        System.out.println("Months: " + period.getMonths());
        System.out.println("Days: " + period.getDays());
        System.out.println("Total months: " + period.toTotalMonths());

        var contractPeriod = Period.of(2, 6, 0);
        var renewalDate = projectStart.plus(contractPeriod);
        System.out.println("\nContract period: " + contractPeriod);
        System.out.println("Renewal date from project start: " + renewalDate);

        var tripled = period.multipliedBy(3);
        var negated = period.negated();
        System.out.println("Period tripled: " + tripled);
        System.out.println("Period negated: " + negated);
        System.out.println("Is negative: " + period.isNegative());
        System.out.println("Is zero: " + Period.ZERO.isZero());
        System.out.println();
    }

    static void periodParsing() {
        System.out.println("=== Period ISO-8601 Parsing ===");

        var oneYear = Period.parse("P1Y");
        var twoMonths = Period.parse("P2M");
        var tenDays = Period.parse("P10D");
        var complex = Period.parse("P1Y6M15D");

        System.out.println("P1Y -> " + oneYear);
        System.out.println("P2M -> " + twoMonths);
        System.out.println("P10D -> " + tenDays);
        System.out.println("P1Y6M15D -> " + complex);

        var fromYears = Period.ofYears(2);
        var fromMonths = Period.ofMonths(18);
        var fromWeeks = Period.ofWeeks(3);
        var normalized = fromMonths.normalized();

        System.out.println("2 years: " + fromYears);
        System.out.println("18 months: " + fromMonths);
        System.out.println("18 months normalized: " + normalized);
        System.out.println("3 weeks: " + fromWeeks);
        System.out.println();
    }

    static void chronoUnitCalculations() {
        System.out.println("=== ChronoUnit Calculations ===");

        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2025, 6, 15);

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        long weeksBetween = ChronoUnit.WEEKS.between(startDate, endDate);
        long monthsBetween = ChronoUnit.MONTHS.between(startDate, endDate);
        long yearsBetween = ChronoUnit.YEARS.between(startDate, endDate);

        System.out.println("From " + startDate + " to " + endDate + ":");
        System.out.println("  Days: " + daysBetween);
        System.out.println("  Weeks: " + weeksBetween);
        System.out.println("  Months: " + monthsBetween);
        System.out.println("  Years: " + yearsBetween);

        var startTime = LocalTime.of(8, 30);
        var endTime = LocalTime.of(17, 45);

        long hoursBetween = ChronoUnit.HOURS.between(startTime, endTime);
        long minutesBetween = ChronoUnit.MINUTES.between(startTime, endTime);
        long secondsBetween = ChronoUnit.SECONDS.between(startTime, endTime);

        System.out.println("\nFrom " + startTime + " to " + endTime + ":");
        System.out.println("  Hours: " + hoursBetween);
        System.out.println("  Minutes: " + minutesBetween);
        System.out.println("  Seconds: " + secondsBetween);

        var startInstant = Instant.parse("2025-01-01T00:00:00Z");
        var endInstant = Instant.parse("2025-03-15T12:30:00Z");
        long millisBetween = ChronoUnit.MILLIS.between(startInstant, endInstant);
        System.out.println("\nMilliseconds between instants: " + millisBetween);

        System.out.println("\nChronoUnit durations:");
        System.out.println("  NANOS: " + ChronoUnit.NANOS.getDuration());
        System.out.println("  MICROS: " + ChronoUnit.MICROS.getDuration());
        System.out.println("  MILLIS: " + ChronoUnit.MILLIS.getDuration());
        System.out.println("  SECONDS: " + ChronoUnit.SECONDS.getDuration());
        System.out.println("  MINUTES: " + ChronoUnit.MINUTES.getDuration());
        System.out.println("  HOURS: " + ChronoUnit.HOURS.getDuration());
        System.out.println("  DAYS: " + ChronoUnit.DAYS.getDuration());
        System.out.println();
    }

    static void builtInTemporalAdjusters() {
        System.out.println("=== Built-in TemporalAdjusters ===");

        var date = LocalDate.of(2025, Month.MARCH, 15);
        System.out.println("Reference date: " + date + " (" + date.getDayOfWeek() + ")");

        System.out.println("First day of month: " + date.with(TemporalAdjusters.firstDayOfMonth()));
        System.out.println("Last day of month: " + date.with(TemporalAdjusters.lastDayOfMonth()));
        System.out.println("First day of year: " + date.with(TemporalAdjusters.firstDayOfYear()));
        System.out.println("Last day of year: " + date.with(TemporalAdjusters.lastDayOfYear()));
        System.out.println("First day of next month: " + date.with(TemporalAdjusters.firstDayOfNextMonth()));
        System.out.println("First day of next year: " + date.with(TemporalAdjusters.firstDayOfNextYear()));

        System.out.println("Next Monday: " + date.with(TemporalAdjusters.next(DayOfWeek.MONDAY)));
        System.out.println("Previous Friday: " + date.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY)));
        System.out.println("Next or same Wednesday: " + date.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY)));
        System.out.println("Previous or same Saturday: " + date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)));

        System.out.println("First Monday of month: " + date.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)));
        System.out.println("Last Friday of month: " + date.with(TemporalAdjusters.lastInMonth(DayOfWeek.FRIDAY)));
        System.out.println("2nd Tuesday of month: " + date.with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.TUESDAY)));
        System.out.println();
    }

    static void customTemporalAdjusters() {
        System.out.println("=== Custom TemporalAdjusters ===");

        var date = LocalDate.of(2025, Month.MARCH, 14);
        System.out.println("Reference date: " + date + " (" + date.getDayOfWeek() + ")");

        TemporalAdjuster nextWorkingDay = temporal -> {
            var d = LocalDate.from(temporal);
            return switch (d.getDayOfWeek()) {
                case FRIDAY -> d.plusDays(3);
                case SATURDAY -> d.plusDays(2);
                default -> d.plusDays(1);
            };
        };

        System.out.println("\nNext working day from:");
        for (int i = 0; i < 7; i++) {
            var d = date.plusDays(i);
            System.out.println("  " + d + " (" + d.getDayOfWeek() + ") -> " + d.with(nextWorkingDay));
        }

        TemporalAdjuster lastDayOfQuarter = temporal -> {
            var d = LocalDate.from(temporal);
            int currentQuarter = (d.getMonthValue() - 1) / 3 + 1;
            var lastMonthOfQuarter = Month.of(currentQuarter * 3);
            return d.withMonth(lastMonthOfQuarter.getValue())
                    .with(TemporalAdjusters.lastDayOfMonth());
        };

        System.out.println("\nLast day of quarter:");
        for (int month = 1; month <= 12; month++) {
            var d = LocalDate.of(2025, month, 15);
            System.out.println("  " + d.getMonth() + " -> " + d.with(lastDayOfQuarter));
        }

        TemporalAdjuster nextPayday = temporal -> {
            var d = LocalDate.from(temporal);
            var fifteenth = d.withDayOfMonth(15);
            var lastDay = d.with(TemporalAdjusters.lastDayOfMonth());

            Temporal payday;
            if (d.getDayOfMonth() < 15) {
                payday = adjustToWorkday(fifteenth);
            } else if (d.getDayOfMonth() < lastDay.getDayOfMonth()) {
                payday = adjustToWorkday(lastDay);
            } else {
                payday = adjustToWorkday(d.plusMonths(1).withDayOfMonth(15));
            }
            return payday;
        };

        System.out.println("\nNext payday (15th or last day of month):");
        var testDates = new LocalDate[]{
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 14),
                LocalDate.of(2025, 3, 15),
                LocalDate.of(2025, 3, 20),
                LocalDate.of(2025, 3, 31),
                LocalDate.of(2025, 2, 27),
        };
        for (var d : testDates) {
            System.out.println("  " + d + " (" + d.getDayOfWeek() + ") -> next payday: " + d.with(nextPayday));
        }
    }

    private static LocalDate adjustToWorkday(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.minusDays(1);
            case SUNDAY -> date.minusDays(2);
            default -> date;
        };
    }
}
