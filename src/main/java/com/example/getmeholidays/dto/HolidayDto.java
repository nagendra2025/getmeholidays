package com.example.getmeholidays.dto;

import java.util.List;

public class HolidayDto {

    private String name;
    private String description;
    private String countryCode;
    private String countryName;
    private String date;       // ISO yyyy-MM-dd
    private List<String> types;

    public HolidayDto() {
    }

    public HolidayDto(String name, String description,
                      String countryCode, String countryName,
                      String date, List<String> types) {
        this.name = name;
        this.description = description;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.date = date;
        this.types = types;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
}

