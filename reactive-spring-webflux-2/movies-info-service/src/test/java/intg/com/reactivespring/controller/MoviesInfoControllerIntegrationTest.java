package com.reactivespring.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntegrationTest {

  @Autowired
  MovieInfoRepository repository;

  @Autowired
  WebTestClient webTestClient;

  static String MOVIES_INFO_URL = "/v1/movieinfos";

  @BeforeEach
  void setUp() {
    var movieInfos = List.of(new MovieInfo(null, "Batman Begins",
                    2005, List.of("Christian Bale", "Michal Cane"), LocalDate.parse("2005-05-15")),
            new MovieInfo(null, "The Dark Night",
                    2008, List.of("Christian Bale", "Heath Ledger"), LocalDate.parse("2008-07-18")),
            new MovieInfo("abc", "Dark Knight Rises",
                    2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-08")));

    repository.saveAll(movieInfos).blockLast();
  }

  @AfterEach
  void tearDown() {
    repository.deleteAll().block();
  }

  @Test
  void addMovieInfo() {
    var movieInfo = new MovieInfo(null, "Batman Begins",
            2005, List.of("Christian Bale", "Michal Cane"), LocalDate.parse("2005-05-15"));
    webTestClient.post()
            .uri(MOVIES_INFO_URL)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(MovieInfo.class)
            .consumeWith(movieInfoEntityExchangeResult -> {
              MovieInfo savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
              assertNotNull(savedMovieInfo);
              assertNotNull(savedMovieInfo.getMovieInfoId());
            });
  }

  @Test
  void getAllMovieInfos() {
    webTestClient.get()
            .uri(MOVIES_INFO_URL)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBodyList(MovieInfo.class)
            .hasSize(3);
  }

  @Test
  void getMovieInfoById() {
    String movieInfoId = "abc";

    webTestClient.get()
            .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(MovieInfo.class)
            .consumeWith(movieInfoEntityExchangeResult -> {
              MovieInfo movieInfo = movieInfoEntityExchangeResult.getResponseBody();
              assertNotNull(movieInfo);
            });
  }

  @Test
  void getMovieInfoById_approach2() {
    String movieInfoId = "abc";

    webTestClient.get()
            .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Dark Knight Rises");

  }

  @Test
  void updateMovieInfo() {
    String movieInfoId = "abc";
    var movieInfo = new MovieInfo(null, "Dark Knight Rises1",
            2005, List.of("Christian Bale", "Michal Cane"), LocalDate.parse("2005-05-15"));

    webTestClient.put()
            .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(MovieInfo.class)
            .consumeWith(movieInfoEntityExchangeResult -> {
              MovieInfo updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
              assertNotNull(updatedMovieInfo);
              assertNotNull(updatedMovieInfo.getMovieInfoId());
              assertEquals("Dark Knight Rises1", updatedMovieInfo.getName());
            });
  }

  @Test
  void deleteMovieInfo() {
    String movieInfoId = "abc";

    webTestClient.delete()
            .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
            .exchange()
            .expectStatus()
            .isNoContent()
            .expectBody(Void.class);
  }

  @Test
  void updateMovieInfo_notFound() {
    String movieInfoId = "def";
    var movieInfo = new MovieInfo(null, "Dark Knight Rises1",
            2005, List.of("Christian Bale", "Michal Cane"), LocalDate.parse("2005-05-15"));

    webTestClient.put()
            .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus()
            .isNotFound();
  }

  @Test
  void getMovieInfoById_notFound() {
    String movieInfoId = "def";

    webTestClient.get()
            .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
            .exchange()
            .expectStatus()
            .isNotFound();
  }
}