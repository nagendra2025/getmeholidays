package com.example.getmeholidays.utils;

import java.util.List;

import com.example.getmeholidays.dto.NationalHolidayDto;

public class HtmlUtility {

    public static String buildHtmlTable(List<NationalHolidayDto> list) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><body>");
        sb.append("<h2>National Holidays</h2>");
        sb.append("<table border='1' cellpadding='6' cellspacing='0' ")
                .append("style='border-collapse:collapse;font-family:Arial;'>");

        // Header row
        sb.append("<tr style='background:#f2f2f2;'>");
        sb.append("<th>Date</th>");
        sb.append("<th>Day</th>");
        sb.append("<th>Purpose</th>");
        sb.append("<th>Type</th>");
        sb.append("</tr>");

        // Data rows
        for (NationalHolidayDto h : list) {
            sb.append("<tr>");
            sb.append("<td>").append(h.getDate()).append("</td>");
            sb.append("<td>").append(h.getDay()).append("</td>");
            sb.append("<td>").append(h.getPurpose()).append("</td>");
            sb.append("<td>").append(h.getType()).append("</td>");
            sb.append("</tr>");
        }

        sb.append("</table></body></html>");
        return sb.toString();
    }
}
