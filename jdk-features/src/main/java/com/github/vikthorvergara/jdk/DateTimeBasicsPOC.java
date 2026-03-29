package com.github.vikthorvergara.jdk;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

public class DateTimeBasicsPOC {

    public static void main(String[] args) {
        localDateManipulation();
        localTimeManipulation();
        localDateTimeManipulation();
        zonedDateTimeAcrossZones();
        typeConversions();
        offsetDateTimeUsage();
        dateComparisons();
        ageCalculation();
        nextDayOfWeekFinder();
    }

    static void localDateManipulation() {
        System.out.println("=== LocalDate Manipulation ===");

        var today = LocalDate.now();
        var specificDate = LocalDate.of(2025, Month.MARCH, 15);
        var parsedDate = LocalDate.parse("2025-07-04");

        System.out.println("Today: " + today);
        System.out.println("Specific date: " + specificDate);
        System.out.println("Parsed date: " + parsedDate);

        var nextWeek = today.plusDays(7);
        var threeMonthsAgo = today.minusMonths(3);
        var firstOfMonth = today.withDayOfMonth(1);
        var endOfYear = today.withMonth(12).withDayOfMonth(31);
        var nextYear = today.plusYears(1);

        System.out.println("Next week: " + nextWeek);
        System.out.println("Three months ago: " + threeMonthsAgo);
        System.out.println("First of current month: " + firstOfMonth);
        System.out.println("End of year: " + endOfYear);
        System.out.println("Next year: " + nextYear);

        System.out.println("Day of week: " + today.getDayOfWeek());
        System.out.println("Day of year: " + today.getDayOfYear());
        System.out.println("Is leap year: " + today.isLeapYear());
        System.out.println("Month length: " + today.lengthOfMonth());
        System.out.println();
    }

    static void localTimeManipulation() {
        System.out.println("=== LocalTime Manipulation ===");

        var now = LocalTime.now();
        var lunchTime = LocalTime.of(12, 30);
        var preciseTime = LocalTime.of(14, 30, 45, 123_456_789);
        var parsedTime = LocalTime.parse("08:15:30");
        var midnight = LocalTime.MIDNIGHT;
        var noon = LocalTime.NOON;

        System.out.println("Now: " + now);
        System.out.println("Lunch time: " + lunchTime);
        System.out.println("Precise time: " + preciseTime);
        System.out.println("Parsed time: " + parsedTime);
        System.out.println("Midnight: " + midnight);
        System.out.println("Noon: " + noon);

        var twoHoursLater = now.plusHours(2);
        var thirtyMinutesBefore = now.minusMinutes(30);

        System.out.println("Two hours later: " + twoHoursLater);
        System.out.println("Thirty minutes before: " + thirtyMinutesBefore);
        System.out.println("Hour: " + now.getHour() + ", Minute: " + now.getMinute());
        System.out.println("Seconds since midnight: " + now.toSecondOfDay());
        System.out.println();
    }

    static void localDateTimeManipulation() {
        System.out.println("=== LocalDateTime Manipulation ===");

        var now = LocalDateTime.now();
        var meeting = LocalDateTime.of(2025, Month.JUNE, 15, 14, 30);
        var combined = LocalDateTime.of(LocalDate.of(2025, 12, 25), LocalTime.of(9, 0));

        System.out.println("Now: " + now);
        System.out.println("Meeting: " + meeting);
        System.out.println("Christmas morning: " + combined);

        var rescheduled = meeting.plusWeeks(2).withHour(10);
        System.out.println("Rescheduled meeting: " + rescheduled);

        System.out.println("Date part: " + now.toLocalDate());
        System.out.println("Time part: " + now.toLocalTime());

        var truncatedToMinutes = now.truncatedTo(ChronoUnit.MINUTES);
        System.out.println("Truncated to minutes: " + truncatedToMinutes);
        System.out.println();
    }

    static void zonedDateTimeAcrossZones() {
        System.out.println("=== ZonedDateTime Across Time Zones ===");

        var saoPaulo = ZoneId.of("America/Sao_Paulo");
        var tokyo = ZoneId.of("Asia/Tokyo");
        var london = ZoneId.of("Europe/London");

        var nowInSaoPaulo = ZonedDateTime.now(saoPaulo);
        var nowInTokyo = ZonedDateTime.now(tokyo);
        var nowInLondon = ZonedDateTime.now(london);

        System.out.println("Sao Paulo: " + nowInSaoPaulo);
        System.out.println("Tokyo:     " + nowInTokyo);
        System.out.println("London:    " + nowInLondon);

        var conferenceInTokyo = ZonedDateTime.of(2025, 9, 20, 10, 0, 0, 0, tokyo);
        var conferenceInSaoPaulo = conferenceInTokyo.withZoneSameInstant(saoPaulo);
        var conferenceInLondon = conferenceInTokyo.withZoneSameInstant(london);

        System.out.println("\nConference starts at 10:00 Tokyo time:");
        System.out.println("  Tokyo:     " + conferenceInTokyo);
        System.out.println("  Sao Paulo: " + conferenceInSaoPaulo);
        System.out.println("  London:    " + conferenceInLondon);

        var sameLocalTimeInLondon = conferenceInTokyo.withZoneSameLocal(london);
        System.out.println("\nSame local time (10:00) but in London zone: " + sameLocalTimeInLondon);

        System.out.println("\nSao Paulo offset: " + nowInSaoPaulo.getOffset());
        System.out.println("Tokyo offset: " + nowInTokyo.getOffset());
        System.out.println("London offset: " + nowInLondon.getOffset());
        System.out.println();
    }

    static void typeConversions() {
        System.out.println("=== Type Conversions ===");

        var localDateTime = LocalDateTime.of(2025, 6, 15, 10, 30);
        System.out.println("LocalDateTime: " + localDateTime);

        var zonedDateTime = localDateTime.atZone(ZoneId.of("America/Sao_Paulo"));
        System.out.println("-> ZonedDateTime (Sao Paulo): " + zonedDateTime);

        var instant = zonedDateTime.toInstant();
        System.out.println("-> Instant: " + instant);

        var epochMillis = instant.toEpochMilli();
        System.out.println("-> Epoch millis: " + epochMillis);

        var backToInstant = Instant.ofEpochMilli(epochMillis);
        var backToZoned = backToInstant.atZone(ZoneId.of("Asia/Tokyo"));
        System.out.println("-> Back to ZonedDateTime (Tokyo): " + backToZoned);

        var backToLocal = backToZoned.toLocalDateTime();
        System.out.println("-> Back to LocalDateTime: " + backToLocal);

        var fromInstant = LocalDateTime.ofInstant(instant, ZoneId.of("Europe/London"));
        System.out.println("-> Instant to LocalDateTime (London): " + fromInstant);
        System.out.println();
    }

    static void offsetDateTimeUsage() {
        System.out.println("=== OffsetDateTime Usage ===");

        var offsetNow = OffsetDateTime.now();
        System.out.println("Current OffsetDateTime: " + offsetNow);

        var saoPauloOffset = ZoneOffset.ofHours(-3);
        var tokyoOffset = ZoneOffset.ofHours(9);

        var meetingInSaoPaulo = OffsetDateTime.of(2025, 8, 20, 14, 0, 0, 0, saoPauloOffset);
        var meetingInTokyo = meetingInSaoPaulo.withOffsetSameInstant(tokyoOffset);

        System.out.println("Meeting in Sao Paulo: " + meetingInSaoPaulo);
        System.out.println("Same instant in Tokyo: " + meetingInTokyo);

        var instant = meetingInSaoPaulo.toInstant();
        System.out.println("As Instant: " + instant);

        var utcMeeting = meetingInSaoPaulo.withOffsetSameInstant(ZoneOffset.UTC);
        System.out.println("In UTC: " + utcMeeting);
        System.out.println();
    }

    static void dateComparisons() {
        System.out.println("=== Date Comparisons ===");

        var date1 = LocalDate.of(2025, 3, 15);
        var date2 = LocalDate.of(2025, 7, 20);
        var date3 = LocalDate.of(2025, 3, 15);

        System.out.println(date1 + " isBefore " + date2 + ": " + date1.isBefore(date2));
        System.out.println(date2 + " isAfter " + date1 + ": " + date2.isAfter(date1));
        System.out.println(date1 + " isEqual " + date3 + ": " + date1.isEqual(date3));

        var time1 = LocalTime.of(9, 30);
        var time2 = LocalTime.of(17, 45);
        System.out.println(time1 + " isBefore " + time2 + ": " + time1.isBefore(time2));

        var zoned1 = ZonedDateTime.of(2025, 6, 15, 10, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));
        var zoned2 = ZonedDateTime.of(2025, 6, 15, 22, 0, 0, 0, ZoneId.of("Asia/Tokyo"));
        System.out.println("\nSao Paulo 10:00 vs Tokyo 22:00:");
        System.out.println("  isBefore: " + zoned1.isBefore(zoned2));
        System.out.println("  isAfter: " + zoned1.isAfter(zoned2));
        System.out.println("  isEqual (same instant): " + zoned1.isEqual(zoned2));
        System.out.println("  Sao Paulo instant: " + zoned1.toInstant());
        System.out.println("  Tokyo instant: " + zoned2.toInstant());
        System.out.println();
    }

    static void ageCalculation() {
        System.out.println("=== Age Calculation ===");

        var birthDate = LocalDate.of(1990, Month.JULY, 15);
        var today = LocalDate.now();

        var age = java.time.Period.between(birthDate, today);
        System.out.println("Birth date: " + birthDate);
        System.out.println("Today: " + today);
        System.out.println("Age: " + age.getYears() + " years, " + age.getMonths() + " months, " + age.getDays() + " days");

        long totalDaysAlive = ChronoUnit.DAYS.between(birthDate, today);
        long totalWeeksAlive = ChronoUnit.WEEKS.between(birthDate, today);
        long totalMonthsAlive = ChronoUnit.MONTHS.between(birthDate, today);

        System.out.println("Total days alive: " + totalDaysAlive);
        System.out.println("Total weeks alive: " + totalWeeksAlive);
        System.out.println("Total months alive: " + totalMonthsAlive);

        var nextBirthday = birthDate.withYear(today.getYear());
        if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
            nextBirthday = nextBirthday.plusYears(1);
        }
        long daysUntilBirthday = ChronoUnit.DAYS.between(today, nextBirthday);
        System.out.println("Next birthday: " + nextBirthday + " (" + daysUntilBirthday + " days away)");
        System.out.println();
    }

    static void nextDayOfWeekFinder() {
        System.out.println("=== Next Day of Week Finder ===");

        var today = LocalDate.now();
        System.out.println("Today: " + today + " (" + today.getDayOfWeek() + ")");

        for (var day : DayOfWeek.values()) {
            var nextOccurrence = today.with(TemporalAdjusters.next(day));
            var nextOrSame = today.with(TemporalAdjusters.nextOrSame(day));
            System.out.println("  Next " + day + ": " + nextOccurrence
                    + " | Next or same: " + nextOrSame);
        }

        var nextFriday = today.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        var previousMonday = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        System.out.println("\nNext Friday: " + nextFriday);
        System.out.println("Previous Monday: " + previousMonday);

        var thirdThursday = LocalDate.of(today.getYear(), today.getMonth(), 1)
                .with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.THURSDAY));
        System.out.println("Third Thursday of current month: " + thirdThursday);

        var lastSunday = LocalDate.of(today.getYear(), today.getMonth(), 1)
                .with(TemporalAdjusters.lastInMonth(DayOfWeek.SUNDAY));
        System.out.println("Last Sunday of current month: " + lastSunday);
    }
}
