package com.example.getmeholidays.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarificResponse {

    private Meta meta;
    private HolidayResponse response;

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public HolidayResponse getResponse() {
        return response;
    }

    public void setResponse(HolidayResponse response) {
        this.response = response;
    }
}

