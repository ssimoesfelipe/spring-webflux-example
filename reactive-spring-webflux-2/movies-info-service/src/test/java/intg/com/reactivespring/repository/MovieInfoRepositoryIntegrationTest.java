package com.reactivespring.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import java.time.LocalDate;
import java.util.List;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntegrationTest {

  @Autowired
  MovieInfoRepository repository;

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
  void findAll() {
    var moviesInfoFlux = repository.findAll().log();

    StepVerifier.create(moviesInfoFlux)
            .expectNextCount(3)
            .verifyComplete();
  }

  @Test
  void findById() {
    var moviesInfoMono = repository.findById("abc").log();

    StepVerifier.create(moviesInfoMono)
            .assertNext(movieInfo -> {
              assertEquals("Dark Knight Rises", movieInfo.getName());
            })
            .verifyComplete();
  }

  @Test
  void saveMovieInfo() {
    MovieInfo movieInfo = new MovieInfo(null, "Batman Begins",
            2005, List.of("Christian Bale", "Michal Cane"), LocalDate.parse("2005-05-15"));

    var moviesInfoMono = repository.save(movieInfo).log();

    StepVerifier.create(moviesInfoMono)
            .assertNext(movieInfo1 -> {
              assertNotNull(movieInfo1.getMovieInfoId());
              assertEquals("Batman Begins", movieInfo1.getName());
            })
            .verifyComplete();
  }

  @Test
  void updateMovieInfo() {
    var movieInfo = repository.findById("abc").block();
    movieInfo.setYear(2021);

    var moviesInfoMono = repository.save(movieInfo).log();

    StepVerifier.create(moviesInfoMono)
            .assertNext(movieInfo1 -> {
              assertEquals(2021, movieInfo1.getYear());
            })
            .verifyComplete();
  }

  @Test
  void deleteMovieInfo() {
    repository.deleteById("abc").block();

    var moviesInfoFlux = repository.findAll().log();

    StepVerifier.create(moviesInfoFlux)
            .expectNextCount(2)
            .verifyComplete();
  }
}