package ru.practicum.rating.service;

import ru.practicum.rating.dto.EventRating;
import ru.practicum.rating.dto.EventsRating;

import java.util.List;

public interface RatingService {

    EventsRating addEventsLike(Long userId, List<Long> eventIds);

    EventRating addEventLike(Long userId, Long eventId);

    EventsRating addEventsDislike(Long userId, List<Long> eventIds);

    EventRating addEventDislike(Long userId, Long eventId);

    void deleteEventsLike(Long userId, List<Long> eventIds);

    void deleteEventLike(Long userId, Long eventId);

    void deleteEventsDislike(Long userId, List<Long> eventIds);

    void deleteEventDislike(Long userId, Long eventId);

}