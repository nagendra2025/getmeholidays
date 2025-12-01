package com.example.getmeholidays.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.getmeholidays.dto.CalendarificResponse;
import com.example.getmeholidays.exception.UpstreamServiceException;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class CalendarificClient {

    private final WebClient webClient;
    private final String apiKey;

    public CalendarificClient(WebClient calendarificWebClient,
                              @Value("${calendarific.api-key}") String apiKey) {

        // 1) Increase max in-memory size (default is 256 KB) – now 2 MB
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) // 2 MB
                )
                .build();

        // 2) Force JSON + use our strategies
        this.webClient = calendarificWebClient.mutate()
                .defaultHeader("Accept", "application/json")
                .exchangeStrategies(strategies)
                .build();

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
                    // 3) Handle 4xx from Calendarific
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("(empty body)")
                                    .flatMap(body -> {
                                        System.err.println("Calendarific 4xx status = " +
                                                clientResponse.statusCode().value());
                                        System.err.println("Calendarific 4xx body   = " + body);

                                        String msg = "Calendarific returned 4xx error: " + body;
                                        return Mono.error(new UpstreamServiceException(
                                                msg,
                                                clientResponse.statusCode().value()
                                        ));
                                    })
                    )
                    // 4) Handle 5xx from Calendarific
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("(empty body)")
                                    .flatMap(body -> {
                                        System.err.println("Calendarific 5xx status = " +
                                                clientResponse.statusCode().value());
                                        System.err.println("Calendarific 5xx body   = " + body);

                                        String msg = "Calendarific returned 5xx error: " + body;
                                        return Mono.error(new UpstreamServiceException(
                                                msg,
                                                clientResponse.statusCode().value()
                                        ));
                                    })
                    )

                    .bodyToMono(CalendarificResponse.class)

                    // 5) Retry on transient network errors like "Connection reset"
                    .retryWhen(
                            Retry.fixedDelay(3, Duration.ofSeconds(1))
                                    .filter(ex -> {
                                        // Retry for low-level network errors
                                        if (ex instanceof WebClientRequestException) {
                                            String msg = ex.getMessage();
                                            return msg != null &&
                                                   msg.toLowerCase().contains("connection reset");
                                        }
                                        return false;
                                    })
                                    .onRetryExhaustedThrow((spec, signal) -> signal.failure())
                    )

                    .block();

        } catch (WebClientResponseException ex) {
            // HTTP error with a response
            System.err.println("WebClientResponseException status = " + ex.getStatusCode().value());
            System.err.println("WebClientResponseException body   = " + ex.getResponseBodyAsString());
            ex.printStackTrace();

            String msg = "Error calling Calendarific: " + ex.getResponseBodyAsString();
            throw new UpstreamServiceException(msg, ex.getStatusCode().value());

        } catch (Exception ex) {
            // Network errors, timeouts, connection reset AFTER retries, etc.
            System.err.println("Unexpected error calling Calendarific:");
            ex.printStackTrace();

            throw new UpstreamServiceException(
                    "Failed to call Calendarific: " + ex.getMessage(),
                    502 // Bad Gateway – upstream failure
            );
        }
    }
}
