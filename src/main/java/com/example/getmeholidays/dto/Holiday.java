package com.example.getmeholidays.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Holiday {

    private String name;
    private String description;
    private Country country;
    private HolidayDate date;
    private List<String> type;

    // New fields from Calendarific JSON

    // "primary_type": "National holiday"
    @JsonProperty("primary_type")
    private String primaryType;

    // "canonical_url": "https://calendarific.com/holiday/..."
    @JsonProperty("canonical_url")
    private String canonicalUrl;

    // "urlid": "united-arab-emirates/islamic-new-year"
    private String urlid;

    // "locations": "All"
    private String locations;

    // "states": can be "All" or a complex object/array
    // Use Object here so Jackson can bind whatever comes back.
    private Object states;

    // ───────── Getters & Setters ─────────

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

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public HolidayDate getDate() {
        return date;
    }

    public void setDate(HolidayDate date) {
        this.date = date;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public String getPrimaryType() {
        return primaryType;
    }

    public void setPrimaryType(String primaryType) {
        this.primaryType = primaryType;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    public String getUrlid() {
        return urlid;
    }

    public void setUrlid(String urlid) {
        this.urlid = urlid;
    }

    public String getLocations() {
        return locations;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }

    public Object getStates() {
        return states;
    }

    public void setStates(Object states) {
        this.states = states;
    }
}
