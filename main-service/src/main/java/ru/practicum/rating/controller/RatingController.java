package ru.practicum.rating.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("/dislike")
    public ResponseEntity<EventsRating> addEventsDislike(@PathVariable Long userId,
                                                         @RequestParam(required = false) List<Long> eventIds) {
        log.info("Received POST-request at /users/{}/dislike?eventIds={} endpoint", userId, eventIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.addEventsDislike(userId, eventIds));
    }

}