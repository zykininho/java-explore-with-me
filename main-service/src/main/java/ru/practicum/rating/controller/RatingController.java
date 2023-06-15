package ru.practicum.rating.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.rating.dto.EventRating;
import ru.practicum.rating.dto.EventsRating;
import ru.practicum.rating.service.RatingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/like")
    public ResponseEntity<EventsRating> addEventsLike(@PathVariable Long userId,
                                                      @RequestParam(required = false) List<Long> eventIds) {
        log.info("Received POST-request at /users/{}/like?eventIds={} endpoint", userId, eventIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.addEventsLike(userId, eventIds));
    }

    @PostMapping("/like")
    public ResponseEntity<EventRating> addEventLike(@PathVariable Long userId,
                                                    @RequestParam(required = false) Long eventId) {
        log.info("Received POST-request at /users/{}/like?eventId={} endpoint", userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.addEventLike(userId, eventId));
    }

    @PostMapping("/dislike")
    public ResponseEntity<EventsRating> addEventsDislike(@PathVariable Long userId,
                                                         @RequestParam(required = false) List<Long> eventIds) {
        log.info("Received POST-request at /users/{}/dislike?eventIds={} endpoint", userId, eventIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.addEventsDislike(userId, eventIds));
    }

    @PostMapping("/dislike")
    public ResponseEntity<EventRating> addEventDislike(@PathVariable Long userId,
                                                       @RequestParam(required = false) Long eventId) {
        log.info("Received POST-request at /users/{}/dislike?eventId={} endpoint", userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.addEventDislike(userId, eventId));
    }

    @DeleteMapping("/like")
    public ResponseEntity<EventsRating> deleteEventsLike(@PathVariable Long userId,
                                                      @RequestParam(required = false) List<Long> eventIds) {
        log.info("Received DELETE-request at /users/{}/like?eventIds={} endpoint", userId, eventIds);
        ratingService.deleteEventsLike(userId, eventIds);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/like")
    public ResponseEntity<EventRating> deleteEventLike(@PathVariable Long userId,
                                                       @RequestParam(required = false) Long eventId) {
        log.info("Received DELETE-request at /users/{}/like?eventId={} endpoint", userId, eventId);
        ratingService.deleteEventLike(userId, eventId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/dislike")
    public ResponseEntity<EventsRating> deleteEventsDislike(@PathVariable Long userId,
                                                         @RequestParam(required = false) List<Long> eventIds) {
        log.info("Received DELETE-request at /users/{}/dislike?eventIds={} endpoint", userId, eventIds);
        ratingService.deleteEventsDislike(userId, eventIds);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/dislike")
    public ResponseEntity<EventRating> deleteEventDislike(@PathVariable Long userId,
                                                          @RequestParam(required = false) Long eventId) {
        log.info("Received DELETE-request at /users/{}/dislike?eventIds={} endpoint", userId, eventId);
        ratingService.deleteEventDislike(userId, eventId);
        return ResponseEntity.noContent().build();
    }

}