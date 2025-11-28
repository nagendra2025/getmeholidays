package com.example.getmeholidays.controller;

import com.example.getmeholidays.dto.HolidayDto;
import com.example.getmeholidays.dto.NationalHolidayDto;
import com.example.getmeholidays.service.HolidayService;
import com.example.getmeholidays.utils.HtmlUtility;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @GetMapping(value = "/holidays")
    public ResponseEntity<?> getHolidays(
            @RequestParam String country,
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String topn) {

        // ─────────────────────────────────────────────
        // NH mode: country + year + topn=NH
        // (must NOT be used with month/day/type)
        // ─────────────────────────────────────────────
        if (topn != null && !topn.isBlank()) {
            List<NationalHolidayDto> nhList =
                    holidayService.getNationalHolidays(country, year, topn, month, day, type);

            // Build HTML table from the NH list
            String htmlTable = HtmlUtility.buildHtmlTable(nhList);

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html")
                    .body(htmlTable);
        }

        // ─────────────────────────────────────────────
        // Normal mode: existing behaviour
        // country + year [+ month] [+ day] [+ type]
        // ─────────────────────────────────────────────
        List<HolidayDto> holidays =
                holidayService.getHolidays(country, year, month, day, type);

        // JSON array of HolidayDto (unchanged from before)
        return ResponseEntity.ok(holidays);
    }
    
    @GetMapping(value = "/help", produces = "text/html")
    public ResponseEntity<String> getHelp() {

        String html = """
            <html>
            <body style="font-family: Arial; line-height: 1.6; padding: 20px;">
                <h1>GetMeHolidays API - Help Page</h1>

                <h2>1. Endpoint: /api/holidays</h2>

                <p><strong>Base URL:</strong> /api/holidays</p>

                <h3>Query Parameters</h3>
                <table border="1" cellpadding="6" cellspacing="0" 
                       style="border-collapse:collapse; font-family:Arial;">
                    <tr style="background:#f2f2f2;">
                        <th>Parameter</th>
                        <th>Required?</th>
                        <th>Description</th>
                    </tr>
                    <tr>
                        <td>country</td>
                        <td>Yes</td>
                        <td>ISO country code (e.g., CA, US, IN)</td>
                    </tr>
                    <tr>
                        <td>year</td>
                        <td>Yes</td>
                        <td>Year of the holidays (2020–2100)</td>
                    </tr>
                    <tr>
                        <td>month</td>
                        <td>No*</td>
                        <td>Month (1–12). Must be used only when <strong>day</strong> is also provided.</td>
                    </tr>
                    <tr>
                        <td>day</td>
                        <td>No*</td>
                        <td>Day of month (1–31). Must be used only with <strong>month</strong>.</td>
                    </tr>
                    <tr>
                        <td>type</td>
                        <td>No</td>
                        <td>Filter by holiday type (optional)</td>
                    </tr>
                    <tr>
                        <td>topn</td>
                        <td>No**</td>
                        <td>
                            Currently supports only <strong>NH</strong>  
                            → returns National Holidays in table format.<br>
                            <strong>Cannot be used with month/day/type</strong>.
                        </td>
                    </tr>
                </table>

                <h2>2. Usage Examples</h2>

                <h3>A) Get all holidays of a country/year</h3>
                <pre>/api/holidays?country=CA&year=2024</pre>

                <h3>B) Get holidays for a specific date</h3>
                <pre>/api/holidays?country=CA&year=2024&month=1&day=1</pre>

                <h3>C) Get only National Holidays (formatted table)</h3>
                <pre>/api/holidays?country=CA&year=2024&topn=NH</pre>

                <h2>3. Rules</h2>
                <ul>
                    <li><strong>topn</strong> cannot be used with <strong>month/day/type</strong></li>
                    <li><strong>day</strong> cannot be used without <strong>month</strong></li>
                    <li>Invalid combinations return helpful error messages</li>
                </ul>

                <hr>
                <p style="font-size: small; color: gray;">
                    GetMeHolidays API © 2025
                </p>
            </body>
            </html>
            """;

        return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(html);
    }

}
