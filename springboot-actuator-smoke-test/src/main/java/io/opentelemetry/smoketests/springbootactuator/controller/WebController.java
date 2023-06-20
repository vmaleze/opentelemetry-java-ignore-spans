package io.opentelemetry.smoketests.springbootactuator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@RestController
public class WebController {
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    private final WebClient webClient;

    public WebController(WebClient webclient) {
        this.webClient = webclient;
    }

    @RequestMapping("/ping")
    public String greeting() {
        logger.info("HTTP request received");
        return "pong";
    }

    @RequestMapping("/client")
    public void client() {
        call();
    }

    private void call() {
        var response = webClient.get().uri("http://localhost:8080/actuator/info").exchange().blockOptional(Duration.ofSeconds(2));
        logger.info("RESPONSE: {}", response.orElse(ClientResponse.create(HttpStatus.CREATED).build()));
    }

}
