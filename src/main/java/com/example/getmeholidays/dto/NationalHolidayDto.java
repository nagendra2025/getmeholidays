package com.example.getmeholidays.dto;


public class NationalHolidayDto {

    // e.g. 2024-07-01
    private String date;

    // e.g. Monday
    private String day;

    // purpose / description of that day
    private String purpose;

    // e.g. "National holiday"
    private String type;

    public NationalHolidayDto() {
    }

    public NationalHolidayDto(String date, String day, String purpose, String type) {
        this.date = date;
        this.day = day;
        this.purpose = purpose;
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public String getDay() {
        return day;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getType() {
        return type;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public void setType(String type) {
        this.type = type;
    }
}

