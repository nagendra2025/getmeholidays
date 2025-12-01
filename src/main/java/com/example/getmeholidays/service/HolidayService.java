package com.example.getmeholidays.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.getmeholidays.client.CalendarificClient;
import com.example.getmeholidays.dto.CalendarificResponse;
import com.example.getmeholidays.dto.Holiday;
import com.example.getmeholidays.dto.HolidayDto;
import com.example.getmeholidays.dto.NationalHolidayDto;
import com.example.getmeholidays.exception.InvalidRequestException;

@Service
public class HolidayService {

    private final CalendarificClient calendarificClient;

    public HolidayService(CalendarificClient calendarificClient) {
        this.calendarificClient = calendarificClient;
    }

    // ─────────────────────────────────────────────
    //  Existing behaviour (no topn) – unchanged
    // ─────────────────────────────────────────────
    public List<HolidayDto> getHolidays(String country,
                                        int year,
                                        Integer month,
                                        Integer day,
                                        String type) {

    	country = country.toUpperCase(Locale.ENGLISH);
        // validate normal mode (no topn here)
        validateRequest(country, year, month, day);

        CalendarificResponse response =
                calendarificClient.getHolidays(country, year, month, day, type);

        if (response == null ||
                response.getResponse() == null ||
                response.getResponse().getHolidays() == null) {
            return Collections.emptyList();
        }

        return response.getResponse().getHolidays().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    //  New NH mode: country + year + topn=NH
    // ─────────────────────────────────────────────
    public List<NationalHolidayDto> getNationalHolidays(String country,
                                                        int year,
                                                        String topn,
                                                        Integer month,
                                                        Integer day,
                                                        String type) {

    	country = country.toUpperCase(Locale.ENGLISH);
        // Basic validation reused for country + year
        validateRequest(country, year, null, null);

        // Business rules for topn / NH mode
        if (topn == null || topn.isBlank()) {
            throw new InvalidRequestException("Parameter 'topn' is required when requesting national holidays.");
        }

        if (!"NH".equalsIgnoreCase(topn)) {
            throw new InvalidRequestException(
                    "Unsupported value for 'topn': " + topn + ". Only 'NH' is supported.");
        }

        if (month != null || day != null) {
            throw new InvalidRequestException(
                    "Parameter 'topn' cannot be used together with 'month' or 'day'. " +
                            "Use either (country, year, month, day) OR (country, year, topn=NH).");
        }

        if (type != null && !type.isBlank()) {
            throw new InvalidRequestException(
                    "Parameter 'type' cannot be used together with 'topn'. " +
                            "Use either 'type' or 'topn', not both.");
        }

        // Call Calendarific without month/day/type and filter in our wrapper
        CalendarificResponse response =
                calendarificClient.getHolidays(country, year, null, null, null);

        if (response == null ||
                response.getResponse() == null ||
                response.getResponse().getHolidays() == null) {
            return Collections.emptyList();
        }

        List<Holiday> allHolidays = response.getResponse().getHolidays();

        return allHolidays.stream()
                // keep only National holidays – using multiple fields
                .filter(this::isNationalHoliday)
                .map(this::toNationalDto)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    //  Shared validation for country/year/month/day
    // ─────────────────────────────────────────────
    private void validateRequest(String country,
                                 int year,
                                 Integer month,
                                 Integer day) {

        if (country == null || country.isBlank()) {
            throw new InvalidRequestException("Country code is required.");
        }
        if (country.length() != 2) {
            throw new InvalidRequestException("Country code must be a 2-letter ISO code, e.g. 'CA'.");
        }

        if (year < 1900 || year > 2100) {
            throw new InvalidRequestException("Year must be between 1900 and 2100.");
        }

        if (month != null) {
            if (month < 1 || month > 12) {
                throw new InvalidRequestException("Invalid month: " + month + ". Allowed range is 1–12.");
            }

            if (day != null) {
                if (day < 1) {
                    throw new InvalidRequestException("Invalid day: " + day + ". Day must be >= 1.");
                }
                try {
                    YearMonth ym = YearMonth.of(year, month);
                    int maxDay = ym.lengthOfMonth();
                    if (day > maxDay) {
                        throw new InvalidRequestException(
                                "Invalid date: " + year + "-" + month + "-" + day +
                                        ". Max day for this month is " + maxDay + ".");
                    }
                } catch (DateTimeException e) {
                    throw new InvalidRequestException("Invalid date values: year=" + year
                            + ", month=" + month + ", day=" + day);
                }
            }
        } else if (day != null) {
            throw new InvalidRequestException("Day cannot be provided without month.");
        }
    }

    // ─────────────────────────────────────────────
    //  Helper: Determine if a holiday is “National holiday”
    //  Checks type, primaryType, description, and name (case-insensitive)
    // ─────────────────────────────────────────────
    private boolean isNationalHoliday(Holiday h) {
        if (h == null) return false;

        String needle = "national holiday";

        // 1) type list
        if (h.getType() != null) {
            boolean inType = h.getType().stream()
                    .filter(Objects::nonNull)
                    .anyMatch(t -> t.toLowerCase(Locale.ENGLISH).contains(needle));
            if (inType) return true;
        }

        // 2) primaryType (new field)
        if (h.getPrimaryType() != null &&
                h.getPrimaryType().toLowerCase(Locale.ENGLISH).contains(needle)) {
            return true;
        }

        // 3) description
        if (h.getDescription() != null &&
                h.getDescription().toLowerCase(Locale.ENGLISH).contains(needle)) {
            return true;
        }

        // 4) name (optional, last resort)
        if (h.getName() != null &&
                h.getName().toLowerCase(Locale.ENGLISH).contains(needle)) {
            return true;
        }

        return false;
    }

    // ─────────────────────────────────────────────
    //  Mapping to full HolidayDto (existing mode)
    // ─────────────────────────────────────────────
    private HolidayDto toDto(Holiday holiday) {
        String countryCode = holiday.getCountry() != null
                ? holiday.getCountry().getId()
                : null;
        String countryName = holiday.getCountry() != null
                ? holiday.getCountry().getName()
                : null;
        String dateIso = holiday.getDate() != null
                ? holiday.getDate().getIso()
                : null;

        return new HolidayDto(
                holiday.getName(),
                holiday.getDescription(),
                countryCode,
                countryName,
                dateIso,
                holiday.getType()
        );
    }

    // ─────────────────────────────────────────────
    //  Mapping to NationalHolidayDto (NH mode)
    // ─────────────────────────────────────────────
    private NationalHolidayDto toNationalDto(Holiday holiday) {
        String iso = holiday.getDate() != null ? holiday.getDate().getIso() : null;

        String dateOnly = extractDatePart(iso);
        String dayName = computeDayOfWeek(iso);

        // Type label – we know it's a National holiday
        String typeLabel = "National holiday";

        return new NationalHolidayDto(
                dateOnly,
                dayName,
                holiday.getDescription(),
                typeLabel
        );
    }

    private String extractDatePart(String iso) {
        if (iso == null) {
            return null;
        }
        // Handles both "2024-03-10" and "2024-03-10T02:00:00-05:00"
        if (iso.length() >= 10) {
            return iso.substring(0, 10);
        }
        return iso;
    }

    private String computeDayOfWeek(String iso) {
        if (iso == null) {
            return null;
        }
        String datePart = extractDatePart(iso);
        try {
            LocalDate date = LocalDate.parse(datePart);
            return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        } catch (DateTimeParseException e) {
            return null; // fallback, or you could throw an exception if you prefer
        }
    }
}
