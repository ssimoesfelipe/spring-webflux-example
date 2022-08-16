package com.reactivespring.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl=http://localhost:8084/v1/movieinfos",
                "restClient.reviewsUrl=http://localhost:8084/v1/reviews"
        }
)
public class MoviesControllerIntgTest {

        @Autowired
        WebTestClient webTestClient;

        @Test
        void retrieveMovieById() {
                var movieId = "abc";
                stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieinfo.json")));

                stubFor(get(urlPathEqualTo("/v1/reviews"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("reviews.json")));

                webTestClient
                        .get()
                        .uri("/v1/movies/{id}", movieId)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(Movie.class)
                        .consumeWith(movieEntityExchangeResult -> {
                                var movie = movieEntityExchangeResult.getResponseBody();
                                assertEquals(2, Objects.requireNonNull(movie).getReviewList().size(), 2);
                                assertEquals("Batman Begins", movie.getMovieInfo().getName());
                        });
        }

        @Test
        void retrieveMovieById_404() {
                var movieId = "abc";
                stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(404)));

                stubFor(get(urlPathEqualTo("/v1/reviews"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("reviews.json")));

                webTestClient
                        .get()
                        .uri("/v1/movies/{id}", movieId)
                        .exchange()
                        .expectStatus()
                        .is4xxClientError();

                WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfos" + "/" + movieId)));
        }

        @Test
        void retrieveMovieById_reviews_404() {
                var movieId = "abc";
                stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieinfo.json")));

                stubFor(get(urlPathEqualTo("/v1/reviews"))
                        .willReturn(aResponse()
                                .withStatus(404)));

                webTestClient
                        .get()
                        .uri("/v1/movies/{id}", movieId)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(Movie.class)
                        .consumeWith(movieEntityExchangeResult -> {
                                var movie = movieEntityExchangeResult.getResponseBody();
                                assertEquals(0, Objects.requireNonNull(movie).getReviewList().size());
                                assertEquals("Batman Begins", movie.getMovieInfo().getName());
                        });
        }

        @Test
        void retrieveMovieById_5xx() {
                var movieId = "abc";
                stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                        .willReturn(aResponse()
                                .withStatus(500)
                                .withBody("MovieInfo Service Unavailable")));

                webTestClient
                        .get()
                        .uri("/v1/movies/{id}", movieId)
                        .exchange()
                        .expectStatus()
                        .is5xxServerError()
                        .expectBody(String.class)
                        .isEqualTo("Server exception in MoviesInfoService MovieInfo Service Unavailable");

                WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movieinfos" + "/" + movieId)));
        }

        @Test
        void retrieveMovieById_reviews_5xx() {
                var movieId = "abc";
                stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieinfo.json")));

                stubFor(get(urlPathEqualTo("/v1/reviews"))
                        .willReturn(aResponse()
                                .withStatus(500)
                                .withBody("Review Service Not Available")));

                webTestClient
                        .get()
                        .uri("/v1/movies/{id}", movieId)
                        .exchange()
                        .expectStatus()
                        .is5xxServerError()
                        .expectBody(String.class)
                        .isEqualTo("Server exception in ReviewsService Review Service Not Available");

                WireMock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews*")));
        }
}
