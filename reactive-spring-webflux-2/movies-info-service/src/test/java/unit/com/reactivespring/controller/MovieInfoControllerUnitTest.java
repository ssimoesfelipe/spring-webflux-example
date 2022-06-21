package com.reactivespring.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.util.List;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MovieInfoControllerUnitTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private MoviesInfoService moviesInfoService;

  static String MOVIES_INFO_URL = "/v1/movieinfos";

  @Test
  void getAllMoviesInfo() {

    List<MovieInfo> movieInfos = List.of(new MovieInfo(null, "Batman Begins",
            2005, List.of("Christian Bale", "Michal Cane"), LocalDate.parse("2005-05-15")),
            new MovieInfo(null, "The Dark Night",
            2008, List.of("Christian Bale", "Heath Ledger"), LocalDate.parse("2008-07-18")),
            new MovieInfo("abc", "Dark Knight Rises",
            2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-08")));

    when(moviesInfoService.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieInfos));

    webTestClient
            .get()
            .uri(MOVIES_INFO_URL)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBodyList(MovieInfo.class)
            .hasSize(3);
  }

  @Test
  void getMovieInfoById() {
    MovieInfo movie = new MovieInfo("abc", "Dark Knight Rises",
            2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-08"));

    when(moviesInfoService.getMovieInfoById(anyString())).thenReturn(Mono.just(movie));

    String movieInfoId = "abc";

    webTestClient
            .get()
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
  void addMovieInfo() {
    var movieInfo = new MovieInfo(null, "Batman Begins",
            2005, List.of("Christian Bale", "Michal Cane"), LocalDate.parse("2005-05-15"));

    when(moviesInfoService.addMovieInfo(any(MovieInfo.class)))
            .thenReturn(Mono.just(new MovieInfo("mockId", "Batman Begins",
                    2005, List.of("Christian Bale", "Michal Cane"), LocalDate.parse("2005-05-15"))));

    webTestClient
            .post()
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
              assertEquals("mockId", savedMovieInfo.getMovieInfoId());
            });
  }

  @Test
  void addMovieInfo_validation() {
    var movieInfo = new MovieInfo(null, "", -2005, List.of(""), LocalDate.parse("2005-05-15"));

    webTestClient
            .post()
            .uri(MOVIES_INFO_URL)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(String.class)
            .consumeWith(stringEntityExchangeResult -> {
              String responseBody = stringEntityExchangeResult.getResponseBody();
              String expectedErrorMessage =
                      "movieInfo.cast must be present, movieInfo.name must be present, movieInfo.year must be a positive value";
              assertNotNull(responseBody);
              assertEquals(expectedErrorMessage, responseBody);
            });
  }

  @Test
  void updateMovieInfo() {
    String movieInfoId = "abc";
    var movieInfo = new MovieInfo(null, "Dark Knight Rises1",
            2005, List.of("Christian Bale", "Michal Cane"), LocalDate.parse("2005-05-15"));

    when(moviesInfoService.updateMovieInfo(any(MovieInfo.class), anyString()))
            .thenReturn(Mono.just(new MovieInfo(movieInfoId, "Dark Knight Rises1",
                    2005, List.of("Christian Bale", "Michal Cane"), LocalDate.parse("2005-05-15"))));

    webTestClient
            .put()
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

    when(moviesInfoService.deleteMovieInfo(anyString())).thenReturn(Mono.empty());

    webTestClient
            .delete()
            .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
            .exchange()
            .expectStatus()
            .isNoContent()
            .expectBody(Void.class);
  }
}
