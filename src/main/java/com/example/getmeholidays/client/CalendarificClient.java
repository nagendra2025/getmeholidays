package com.example.getmeholidays.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.getmeholidays.dto.CalendarificResponse;
import com.example.getmeholidays.exception.UpstreamServiceException;

import reactor.core.publisher.Mono;

@Component
public class CalendarificClient {

    private final WebClient webClient;
    private final String apiKey;

    public CalendarificClient(WebClient calendarificWebClient,
                              @Value("${calendarific.api-key}") String apiKey) {
        this.webClient = calendarificWebClient;
        this.apiKey = apiKey;
    }

    public CalendarificResponse getHolidays(String country,
                                            int year,
                                            Integer month,
                                            Integer day,
                                            String type) {

        WebClient.RequestHeadersSpec<?> headersSpec = webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/holidays")
                            .queryParam("api_key", apiKey)
                            .queryParam("country", country)
                            .queryParam("year", year);

                    if (month != null) uriBuilder.queryParam("month", month);
                    if (day != null) uriBuilder.queryParam("day", day);
                    if (type != null && !type.isBlank()) uriBuilder.queryParam("type", type);

                    return uriBuilder.build();
                });

        try {
            return headersSpec
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        String msg = "Calendarific returned 4xx error: " + body;
                                        return Mono.error(new UpstreamServiceException(
                                                msg,
                                                clientResponse.statusCode().value()
                                        ));
                                    })
                    )
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        String msg = "Calendarific returned 5xx error: " + body;
                                        return Mono.error(new UpstreamServiceException(
                                                msg,
                                                clientResponse.statusCode().value()
                                        ));
                                    })
                    )
                    .bodyToMono(CalendarificResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            // Extra safety – in case retrieve().onStatus is bypassed for some reason
            String msg = "Error calling Calendarific: " + ex.getResponseBodyAsString();
            // ✅ use getStatusCode().value() instead of getRawStatusCode()
            throw new UpstreamServiceException(msg, ex.getStatusCode().value());
        } catch (Exception ex) {
            // Network errors, timeouts, etc.
            throw new UpstreamServiceException("Failed to call Calendarific: " + ex.getMessage(), 500);
        }
    }
}
