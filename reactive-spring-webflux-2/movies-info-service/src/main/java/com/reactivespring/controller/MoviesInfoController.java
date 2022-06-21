package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
public class MoviesInfoController {

  private final MoviesInfoService moviesInfoService;

  public MoviesInfoController(MoviesInfoService moviesInfoService) {
    this.moviesInfoService = moviesInfoService;
  }

  @GetMapping("/movieinfos")
  public Flux<MovieInfo> getAllMovieInfos() {
    return moviesInfoService.getAllMovieInfos().log();
  }

  @GetMapping("/movieinfos/{id}")
  public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String id) {
    return moviesInfoService.getMovieInfoById(id)
            .map(movieInfo -> ResponseEntity.ok().body(movieInfo))
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
  }

  @PostMapping("/movieinfos")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
    return moviesInfoService.addMovieInfo(movieInfo).log();
  }

  @PutMapping("/movieinfos/{id}")
  public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo updateMovieInfo, @PathVariable String id) {
    return moviesInfoService.updateMovieInfo(updateMovieInfo, id)
            .map(movieInfo -> ResponseEntity.ok().body(movieInfo))
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
  }

  @DeleteMapping("/movieinfos/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteMovieInfo(@PathVariable String id) {
    return moviesInfoService.deleteMovieInfo(id);
  }
}
