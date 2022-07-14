package com.reactivespring.routes;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

  @Autowired
  WebTestClient webTestClient;

  @Autowired
  ReviewReactiveRepository repository;

  static String REVIEWS_URL = "/v1/reviews";

  @BeforeEach
  void setUp() {
    var reviewsList = List.of(
            new Review(null, 1L, "Awesome Movie", 9.0),
            new Review(null, 1L, "Awesome Movie1", 9.0),
            new Review(null, 2L, "Excellent Movie", 8.0));
    repository.saveAll(reviewsList).blockLast();
  }

  @AfterEach
  void tearDown() {
    repository.deleteAll().block();
  }

  @Test
  void addReview() {

    var newReview = new Review(null, 2L, "Excellent Movie", 8.0);

    webTestClient
            .post()
            .uri(REVIEWS_URL)
            .bodyValue(newReview)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Review.class)
            .consumeWith(reviewEntityExchangeResult -> {
              var savedReview = reviewEntityExchangeResult.getResponseBody();

              assertNotNull(savedReview);
              assertNotNull(savedReview.getReviewId());

            });
  }
}
