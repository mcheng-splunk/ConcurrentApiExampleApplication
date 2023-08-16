package com.splunk.java.concurrentapiexampleapplication.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
public class ConcurrentApiController {

    private final WebClient boredApiClient;
    private final WebClient pokemonApiClient;

    public ConcurrentApiController(WebClient.Builder webClientBuilder) {
        this.boredApiClient = webClientBuilder.baseUrl("https://www.boredapi.com/api/").build();
        this.pokemonApiClient = webClientBuilder.baseUrl("https://pokeapi.co/api/v2/").build();

    }

    @Async
    @GetMapping("/async")
    public CompletableFuture<String> performAsyncProcessing() {
        log.info("Async processing started");

        // Make external API calls asynchronously
        Mono<String> result1 = callExternalApi(boredApiClient, "/activity");
        Mono<String> result2 = callExternalApi(boredApiClient,"/activity");

        // Make external API calls asynchronously
        Mono<String> boredApiResult = callExternalApi(boredApiClient, "/activity");
        Mono<String> pokemonApiResult = callExternalApiWithDelay(pokemonApiClient, "/pokemon/1", Duration.ofSeconds(5));



        // Start processing the results in a separate thread
        CompletableFuture.runAsync(() -> processResults(boredApiResult.block(), pokemonApiResult.block()));

        // Return a response immediately
        return CompletableFuture.completedFuture("Async processing started. Results will be processed in the background.");
    }

    private Mono<String> callExternalApi(WebClient webClient, String path) {
        return webClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(String.class);
    }
    private Mono<String> callExternalApiWithDelay(WebClient webClient, String path, Duration delay) {
        return webClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(String.class)
                .delaySubscription(delay);
    }

    private void processResults(String boredApiResult, String pokemonApiResult) {
        log.info("Results received: {} and {}", boredApiResult, pokemonApiResult);

        // Perform some simple processing on the results
        String processedResult = "Processed: " + boredApiResult + " - " + pokemonApiResult;
        log.info("Processed result: {}", processedResult);
    }
}
