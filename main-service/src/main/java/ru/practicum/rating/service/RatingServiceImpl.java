package ru.practicum.rating.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.rating.dto.EventRating;
import ru.practicum.rating.dto.EventTopRating;
import ru.practicum.rating.dto.EventsRating;
import ru.practicum.rating.dto.UserTopRating;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    @Override
    public EventsRating addEventsLike(Long userId, List<Long> eventIds) {
        return null;
    }

    @Override
    public EventRating addEventLike(Long userId, Long eventId) {
        return null;
    }

    @Override
    public EventsRating addEventsDislike(Long userId, List<Long> eventIds) {
        return null;
    }

    @Override
    public EventRating addEventDislike(Long userId, Long eventId) {
        return null;
    }

    @Override
    public void deleteEventsLike(Long userId, List<Long> eventIds) {

    }

    @Override
    public void deleteEventLike(Long userId, Long eventId) {

    }

    @Override
    public void deleteEventsDislike(Long userId, List<Long> eventIds) {

    }

    @Override
    public void deleteEventDislike(Long userId, Long eventId) {

    }

    @Override
    public EventTopRating getEventRating(Integer top) {
        return null;
    }

    @Override
    public UserTopRating getUserRating(Integer top) {
        return null;
    }

}