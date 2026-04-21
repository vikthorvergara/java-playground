package com.github.vikthorvergara.jdk.basics;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class JodaTimePOC {

    static void dateTimeBasics() {
        DateTime joda = new DateTime(2026, 4, 20, 10, 30, DateTimeZone.UTC);
        System.out.println("joda DateTime -> " + joda);

        ZonedDateTime jdk = ZonedDateTime.of(2026, 4, 20, 10, 30, 0, 0, ZoneId.of("UTC"));
        System.out.println("java ZonedDateTime -> " + jdk);

        System.out.println("joda plusDays(5) -> " + joda.plusDays(5));
        System.out.println("java plusDays(5) -> " + jdk.plusDays(5));
    }

    static void localDateAndTime() {
        LocalDate jodaDate = new LocalDate(2026, 4, 20);
        java.time.LocalDate javaDate = java.time.LocalDate.of(2026, 4, 20);
        System.out.println("joda LocalDate -> " + jodaDate);
        System.out.println("java LocalDate -> " + javaDate);

        LocalTime jodaTime = new LocalTime(14, 30, 0);
        java.time.LocalTime javaTime = java.time.LocalTime.of(14, 30, 0);
        System.out.println("joda LocalTime -> " + jodaTime);
        System.out.println("java LocalTime -> " + javaTime);

        System.out.println("joda date.withDayOfMonth(1) -> " + jodaDate.withDayOfMonth(1));
        System.out.println("java date.withDayOfMonth(1) -> " + javaDate.withDayOfMonth(1));
    }

    static void periodAndDuration() {
        LocalDate start = new LocalDate(2026, 1, 1);
        LocalDate end = new LocalDate(2026, 4, 20);

        Period jodaPeriod = new Period(start, end);
        System.out.println("joda Period(Jan1 - Apr20) -> " + jodaPeriod);

        java.time.Period javaPeriod = java.time.Period.between(
                java.time.LocalDate.of(2026, 1, 1),
                java.time.LocalDate.of(2026, 4, 20));
        System.out.println("java Period(Jan1 - Apr20) -> " + javaPeriod);

        int jodaDays = Days.daysBetween(start, end).getDays();
        long javaDays = ChronoUnit.DAYS.between(
                java.time.LocalDate.of(2026, 1, 1),
                java.time.LocalDate.of(2026, 4, 20));
        System.out.println("joda Days.daysBetween -> " + jodaDays);
        System.out.println("java ChronoUnit.DAYS.between -> " + javaDays);

        Duration jodaDur = Duration.standardHours(5).plus(Duration.standardMinutes(30));
        java.time.Duration javaDur = java.time.Duration.ofHours(5).plusMinutes(30);
        System.out.println("joda Duration 5h30m -> " + jodaDur.getStandardMinutes() + " min");
        System.out.println("java Duration 5h30m -> " + javaDur.toMinutes() + " min");
    }

    static void intervals() {
        DateTime a = new DateTime(2026, 1, 1, 0, 0, DateTimeZone.UTC);
        DateTime b = new DateTime(2026, 4, 20, 0, 0, DateTimeZone.UTC);

        Interval jodaInterval = new Interval(a, b);
        System.out.println("joda Interval -> " + jodaInterval);
        System.out.println("joda Interval.contains(Feb1) -> "
                + jodaInterval.contains(new DateTime(2026, 2, 1, 0, 0, DateTimeZone.UTC)));

        var ja = java.time.Instant.parse("2026-01-01T00:00:00Z");
        var jb = java.time.Instant.parse("2026-04-20T00:00:00Z");
        var jMid = java.time.Instant.parse("2026-02-01T00:00:00Z");
        boolean contains = !jMid.isBefore(ja) && jMid.isBefore(jb);
        System.out.println("java equivalent contains(Feb1) -> " + contains);
    }

    static void formatters() {
        DateTime joda = new DateTime(2026, 4, 20, 23, 7, 41, DateTimeZone.UTC);
        var jodaFmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("joda format -> " + jodaFmt.print(joda));

        ZonedDateTime jdk = ZonedDateTime.of(2026, 4, 20, 23, 7, 41, 0, ZoneId.of("UTC"));
        var javaFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("java format -> " + jdk.format(javaFmt));

        DateTime jodaParsed = jodaFmt.parseDateTime("2026-04-20 23:07:41");
        System.out.println("joda parse -> " + jodaParsed);

        var javaParsed = java.time.LocalDateTime.parse("2026-04-20 23:07:41", javaFmt);
        System.out.println("java parse -> " + javaParsed);
    }

    public static void main(String[] args) {
        System.out.println("--- DateTime basics ---");
        dateTimeBasics();

        System.out.println("\n--- LocalDate and LocalTime ---");
        localDateAndTime();

        System.out.println("\n--- Period and Duration ---");
        periodAndDuration();

        System.out.println("\n--- Interval ---");
        intervals();

        System.out.println("\n--- formatters ---");
        formatters();
    }
}
