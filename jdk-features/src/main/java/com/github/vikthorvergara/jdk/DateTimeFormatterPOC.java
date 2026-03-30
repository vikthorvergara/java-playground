package com.github.vikthorvergara.jdk;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.HijrahDate;
import java.time.chrono.JapaneseDate;
import java.time.chrono.ThaiBuddhistDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;

public class DateTimeFormatterPOC {

    public static void main(String[] args) {
        patternFormatting();
        isoFormats();
        parsingStrings();
        localizedFormatting();
        daylightSavingTransitions();
        calendarSystems();
        clockUsage();
        optionalSectionsParsing();
    }

    static void patternFormatting() {
        System.out.println("=== Pattern Formatting ===");

        var dateTime = LocalDateTime.of(2025, 7, 15, 14, 30, 45);

        var patterns = new String[]{
                "yyyy-MM-dd",
                "dd/MM/yyyy",
                "MM-dd-yyyy",
                "yyyy-MM-dd HH:mm:ss",
                "dd MMM yyyy",
                "EEEE, dd 'de' MMMM 'de' yyyy",
                "HH:mm:ss",
                "hh:mm a",
                "yyyy'T'HH:mm:ss",
                "E, MMM dd yyyy",
                "QQQQ yyyy",
                "D'th day of' yyyy",
                "w'th week of' yyyy",
        };

        for (var pattern : patterns) {
            var formatter = DateTimeFormatter.ofPattern(pattern);
            System.out.println("  " + pattern + " -> " + dateTime.format(formatter));
        }
        System.out.println();
    }

    static void isoFormats() {
        System.out.println("=== ISO Formats ===");

        var date = LocalDate.of(2025, 7, 15);
        var time = LocalTime.of(14, 30, 45);
        var dateTime = LocalDateTime.of(date, time);
        var zonedDateTime = dateTime.atZone(ZoneId.of("America/Sao_Paulo"));
        var instant = zonedDateTime.toInstant();

        System.out.println("ISO_LOCAL_DATE: " + date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        System.out.println("ISO_LOCAL_TIME: " + time.format(DateTimeFormatter.ISO_LOCAL_TIME));
        System.out.println("ISO_LOCAL_DATE_TIME: " + dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        System.out.println("ISO_ZONED_DATE_TIME: " + zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        System.out.println("ISO_OFFSET_DATE_TIME: " + zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        System.out.println("ISO_INSTANT: " + DateTimeFormatter.ISO_INSTANT.format(instant));
        System.out.println("ISO_DATE: " + zonedDateTime.format(DateTimeFormatter.ISO_DATE));
        System.out.println("ISO_ORDINAL_DATE: " + date.format(DateTimeFormatter.ISO_ORDINAL_DATE));
        System.out.println("ISO_WEEK_DATE: " + date.format(DateTimeFormatter.ISO_WEEK_DATE));
        System.out.println("BASIC_ISO_DATE: " + date.format(DateTimeFormatter.BASIC_ISO_DATE));
        System.out.println("RFC_1123_DATE_TIME: " + zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        System.out.println();
    }

    static void parsingStrings() {
        System.out.println("=== Parsing Strings ===");

        var isoDate = LocalDate.parse("2025-07-15");
        System.out.println("ISO date parse: " + isoDate);

        var customFormatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        var brDate = LocalDate.parse("15/07/2025", customFormatter1);
        System.out.println("BR date parse (dd/MM/yyyy): " + brDate);

        var customFormatter2 = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
        var usDateTime = LocalDateTime.parse("07-15-2025 14:30", customFormatter2);
        System.out.println("US datetime parse (MM-dd-yyyy HH:mm): " + usDateTime);

        var customFormatter3 = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
        var parsed3 = LocalDateTime.parse("15 Jul 2025 14:30:45", customFormatter3);
        System.out.println("Named month parse: " + parsed3);

        var zonedFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss VV");
        var parsedZoned = ZonedDateTime.parse("2025-07-15 14:30:45 America/Sao_Paulo", zonedFormatter);
        System.out.println("Zoned parse: " + parsedZoned);

        var time = LocalTime.parse("14:30:45");
        System.out.println("Time parse: " + time);

        var caseInsensitive = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("dd-MMM-yyyy")
                .toFormatter(Locale.ENGLISH);
        var parsedInsensitive = LocalDate.parse("15-JUL-2025", caseInsensitive);
        System.out.println("Case insensitive parse: " + parsedInsensitive);
        System.out.println();
    }

    static void localizedFormatting() {
        System.out.println("=== Localized Formatting ===");

        var dateTime = LocalDateTime.of(2025, 7, 15, 14, 30, 45);
        var zonedDateTime = dateTime.atZone(ZoneId.of("America/Sao_Paulo"));

        var ptBR = Locale.forLanguageTag("pt-BR");
        var enUS = Locale.forLanguageTag("en-US");
        var jaJP = Locale.forLanguageTag("ja-JP");
        var deDE = Locale.forLanguageTag("de-DE");
        var frFR = Locale.forLanguageTag("fr-FR");

        var locales = new Locale[]{ptBR, enUS, jaJP, deDE, frFR};

        System.out.println("FormatStyle.FULL:");
        var fullFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);
        for (var locale : locales) {
            System.out.println("  " + locale + ": " + zonedDateTime.format(fullFormatter.withLocale(locale)));
        }

        System.out.println("\nFormatStyle.LONG:");
        var longFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG);
        for (var locale : locales) {
            System.out.println("  " + locale + ": " + zonedDateTime.format(longFormatter.withLocale(locale)));
        }

        System.out.println("\nFormatStyle.MEDIUM:");
        var mediumFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        for (var locale : locales) {
            System.out.println("  " + locale + ": " + zonedDateTime.format(mediumFormatter.withLocale(locale)));
        }

        System.out.println("\nFormatStyle.SHORT:");
        var shortFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        for (var locale : locales) {
            System.out.println("  " + locale + ": " + zonedDateTime.format(shortFormatter.withLocale(locale)));
        }

        System.out.println("\nDay/Month names in different locales:");
        for (var locale : locales) {
            var dayName = dateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, locale);
            var monthName = dateTime.getMonth().getDisplayName(TextStyle.FULL, locale);
            System.out.println("  " + locale + ": " + dayName + ", " + monthName);
        }
        System.out.println();
    }

    static void daylightSavingTransitions() {
        System.out.println("=== Daylight Saving Time Transitions ===");

        var saoPaulo = ZoneId.of("America/Sao_Paulo");
        var rules = saoPaulo.getRules();

        System.out.println("Zone: " + saoPaulo);
        System.out.println("Current rules: " + rules);

        var transitions = rules.getTransitions();
        System.out.println("Historical transitions (last 5):");
        int start = Math.max(0, transitions.size() - 5);
        for (int i = start; i < transitions.size(); i++) {
            var transition = transitions.get(i);
            System.out.println("  " + transition);
            System.out.println("    Before: " + transition.getDateTimeBefore()
                    + " (offset " + transition.getOffsetBefore() + ")");
            System.out.println("    After:  " + transition.getDateTimeAfter()
                    + " (offset " + transition.getOffsetAfter() + ")");
            System.out.println("    Gap/Overlap: " + (transition.isGap() ? "GAP" : "OVERLAP")
                    + " of " + transition.getDuration());
        }

        var transitionRules = rules.getTransitionRules();
        System.out.println("\nRecurring transition rules: " + transitionRules.size());
        for (var rule : transitionRules) {
            System.out.println("  " + rule);
        }

        System.out.println("\nNew York DST (well-known transitions):");
        var newYork = ZoneId.of("America/New_York");

        var beforeSpringForward = ZonedDateTime.of(2025, 3, 9, 1, 30, 0, 0, newYork);
        var afterSpringForward = ZonedDateTime.of(2025, 3, 9, 3, 30, 0, 0, newYork);
        System.out.println("  Before spring forward: " + beforeSpringForward);
        System.out.println("  After spring forward:  " + afterSpringForward);
        System.out.println("  Duration between: " + Duration.between(beforeSpringForward, afterSpringForward));

        var gapTime = LocalDateTime.of(2025, 3, 9, 2, 30, 0);
        var resolvedGap = gapTime.atZone(newYork);
        System.out.println("  2:30 AM during gap resolves to: " + resolvedGap);

        var beforeFallBack = ZonedDateTime.of(2025, 11, 2, 0, 30, 0, 0, newYork);
        var afterFallBack = beforeFallBack.plusHours(2);
        System.out.println("\n  Before fall back: " + beforeFallBack);
        System.out.println("  After +2 hours:   " + afterFallBack);

        var overlapTime = LocalDateTime.of(2025, 11, 2, 1, 30, 0);
        var resolvedOverlapEarlier = overlapTime.atZone(newYork).withEarlierOffsetAtOverlap();
        var resolvedOverlapLater = overlapTime.atZone(newYork).withLaterOffsetAtOverlap();
        System.out.println("  1:30 AM during overlap (earlier): " + resolvedOverlapEarlier);
        System.out.println("  1:30 AM during overlap (later):   " + resolvedOverlapLater);
        System.out.println("  Same instant? " + resolvedOverlapEarlier.isEqual(resolvedOverlapLater));
        System.out.println();
    }

    static void calendarSystems() {
        System.out.println("=== Calendar Systems ===");

        var today = LocalDate.now();
        System.out.println("ISO (Gregorian): " + today);

        var japaneseDate = JapaneseDate.from(today);
        System.out.println("Japanese: " + japaneseDate);
        System.out.println("Japanese era: " + japaneseDate.getEra());
        var japaneseFormatter = DateTimeFormatter.ofPattern("G yyyy-MM-dd")
                .withChronology(japaneseDate.getChronology());
        System.out.println("Japanese formatted: " + japaneseFormatter.format(japaneseDate));

        var hijrahDate = HijrahDate.from(today);
        System.out.println("\nHijrah (Islamic): " + hijrahDate);
        System.out.println("Hijrah era: " + hijrahDate.getEra());
        var hijrahFormatter = DateTimeFormatter.ofPattern("G yyyy-MM-dd")
                .withChronology(hijrahDate.getChronology());
        System.out.println("Hijrah formatted: " + hijrahFormatter.format(hijrahDate));

        var thaiDate = ThaiBuddhistDate.from(today);
        System.out.println("\nThai Buddhist: " + thaiDate);
        System.out.println("Thai era: " + thaiDate.getEra());
        var thaiFormatter = DateTimeFormatter.ofPattern("G yyyy-MM-dd")
                .withChronology(thaiDate.getChronology());
        System.out.println("Thai formatted: " + thaiFormatter.format(thaiDate));

        var specificJapanese = JapaneseDate.of(2019, 5, 1);
        System.out.println("\nNew Reiwa era start: " + specificJapanese);
        var reiwaFormatter = DateTimeFormatter.ofPattern("GGGG y'nen' M'gatsu' d'nichi'",
                Locale.forLanguageTag("ja-JP"))
                .withChronology(specificJapanese.getChronology());
        System.out.println("Reiwa formatted: " + reiwaFormatter.format(specificJapanese));
        System.out.println();
    }

    static void clockUsage() {
        System.out.println("=== Clock Usage ===");

        var systemClock = Clock.systemDefaultZone();
        System.out.println("System clock: " + systemClock);
        System.out.println("System instant: " + systemClock.instant());

        var utcClock = Clock.systemUTC();
        System.out.println("\nUTC clock: " + utcClock);
        System.out.println("UTC instant: " + utcClock.instant());

        var tokyoClock = Clock.system(ZoneId.of("Asia/Tokyo"));
        System.out.println("\nTokyo clock: " + tokyoClock);
        System.out.println("Tokyo time: " + LocalDateTime.now(tokyoClock));

        var fixedInstant = Instant.parse("2025-12-25T10:00:00Z");
        var fixedClock = Clock.fixed(fixedInstant, ZoneId.of("America/Sao_Paulo"));
        System.out.println("\nFixed clock (Christmas): " + fixedClock);
        System.out.println("Fixed date: " + LocalDate.now(fixedClock));
        System.out.println("Fixed time: " + LocalTime.now(fixedClock));
        System.out.println("Fixed datetime: " + LocalDateTime.now(fixedClock));
        System.out.println("Always same: " + LocalDateTime.now(fixedClock));

        var offsetClock = Clock.offset(Clock.systemUTC(), Duration.ofHours(-5));
        System.out.println("\nOffset clock (UTC-5h): " + offsetClock);
        System.out.println("Offset time: " + LocalDateTime.now(offsetClock));

        var tickClock = Clock.tickSeconds(ZoneId.of("Europe/London"));
        System.out.println("\nTick clock (second precision): " + tickClock);
        System.out.println("Tick instant: " + tickClock.instant());

        var tickMinutesClock = Clock.tickMinutes(ZoneId.of("Europe/London"));
        System.out.println("Tick minutes clock: " + tickMinutesClock);
        System.out.println("Tick minutes instant: " + tickMinutesClock.instant());

        System.out.println("\nUsing fixed clock for predictable testing:");
        var testClock = Clock.fixed(Instant.parse("2025-01-15T09:00:00Z"), ZoneId.of("UTC"));
        var invoiceDate = LocalDate.now(testClock);
        var dueDate = invoiceDate.plusDays(30);
        System.out.println("Invoice date: " + invoiceDate);
        System.out.println("Due date (net 30): " + dueDate);
        System.out.println();
    }

    static void optionalSectionsParsing() {
        System.out.println("=== Optional Sections Formatting/Parsing ===");

        var formatterWithOptionalTime = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd")
                .optionalStart()
                .appendPattern(" HH:mm")
                .optionalStart()
                .appendPattern(":ss")
                .optionalEnd()
                .optionalEnd()
                .toFormatter();

        var dateOnly = "2025-07-15";
        var dateWithTime = "2025-07-15 14:30";
        var dateWithFullTime = "2025-07-15 14:30:45";

        var parsedDateOnly = formatterWithOptionalTime.parseBest(
                dateOnly, LocalDateTime::from, LocalDate::from);
        var parsedWithTime = formatterWithOptionalTime.parseBest(
                dateWithTime, LocalDateTime::from, LocalDate::from);
        var parsedFull = formatterWithOptionalTime.parseBest(
                dateWithFullTime, LocalDateTime::from, LocalDate::from);

        System.out.println("'" + dateOnly + "' -> " + parsedDateOnly + " [" + parsedDateOnly.getClass().getSimpleName() + "]");
        System.out.println("'" + dateWithTime + "' -> " + parsedWithTime + " [" + parsedWithTime.getClass().getSimpleName() + "]");
        System.out.println("'" + dateWithFullTime + "' -> " + parsedFull + " [" + parsedFull.getClass().getSimpleName() + "]");

        var formatterWithDefaults = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd")
                .optionalStart()
                .appendPattern(" HH:mm:ss")
                .optionalEnd()
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter();

        var alwaysDateTime1 = LocalDateTime.parse("2025-07-15", formatterWithDefaults);
        var alwaysDateTime2 = LocalDateTime.parse("2025-07-15 14:30:45", formatterWithDefaults);
        System.out.println("\nWith defaults - date only: " + alwaysDateTime1);
        System.out.println("With defaults - full: " + alwaysDateTime2);

        var optionalZoneFormatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")
                .optionalStart()
                .appendLiteral('[')
                .appendZoneId()
                .appendLiteral(']')
                .optionalEnd()
                .toFormatter();

        var withZone = "2025-07-15 14:30:45[America/Sao_Paulo]";
        var withoutZone = "2025-07-15 14:30:45";

        var parsedWithZone = optionalZoneFormatter.parseBest(
                withZone, ZonedDateTime::from, LocalDateTime::from);
        var parsedWithoutZone = optionalZoneFormatter.parseBest(
                withoutZone, ZonedDateTime::from, LocalDateTime::from);

        System.out.println("\nOptional zone - with zone: " + parsedWithZone + " [" + parsedWithZone.getClass().getSimpleName() + "]");
        System.out.println("Optional zone - without: " + parsedWithoutZone + " [" + parsedWithoutZone.getClass().getSimpleName() + "]");

        System.out.println("\nMulti-format parsing:");
        var multiFormatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                .toFormatter();

        var formats = new String[]{"2025-07-15", "15/07/2025", "07-15-2025"};
        for (var input : formats) {
            var parsed = LocalDate.parse(input, multiFormatter);
            System.out.println("  '" + input + "' -> " + parsed);
        }
    }
}
