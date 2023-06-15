package ru.practicum.rating.service;

import ru.practicum.rating.dto.EventsRating;

import java.util.List;

public interface RatingService {

    EventsRating addEventsLike(Long userId, List<Long> eventIds);

    EventsRating addEventsDislike(Long userId, List<Long> eventIds);

}