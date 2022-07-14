package com.reactivespring.routes;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

  @MockBean
  private ReviewReactiveRepository repository;

  @Autowired
  WebTestClient webTestClient;

  static String REVIEWS_URL = "/v1/reviews";

  @Test
  void addReview() {

    var newReview = new Review(null, 2L, "Excellent Movie", 8.0);

    when(repository.save(isA(Review.class)))
            .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

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

  @Test
  void addReview_validation() {

    var newReview = new Review(null, null, "Excellent Movie", -8.0);

    when(repository.save(isA(Review.class)))
            .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

    webTestClient
            .post()
            .uri(REVIEWS_URL)
            .bodyValue(newReview)
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(String.class)
            .isEqualTo("rating.movieInfoId: must not be null, rating.negative : rating is negative and please pass a non-negative value");
  }
}
