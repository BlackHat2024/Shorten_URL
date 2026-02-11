package com.shorten_url;

import com.shorten_url.service.UrlService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@SpringBootApplication
public class ShortenUrlApplication {

    public ShortenUrlApplication(UrlService urlService) {
        this.urlService = urlService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ShortenUrlApplication.class, args);
    }

    private final UrlService urlService;

    @Bean
    public RouterFunction<ServerResponse> shortUrlRedirectRoute() {
        return route(GET("/{code}"), request -> {
            String code = request.pathVariable("code");

            if (!code.matches("^[0-9A-Za-z]{8}$")) {
                return ServerResponse.status(HttpStatus.NOT_FOUND).build();
            }

            String original = urlService.resolveByCode(code).getOriginalUrl();
            return ServerResponse.status(HttpStatus.FOUND)
                    .location(URI.create(original))
                    .build();
        });
    }
}
