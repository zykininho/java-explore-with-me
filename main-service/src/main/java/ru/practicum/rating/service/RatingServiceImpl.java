package ru.practicum.rating.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.rating.dto.EventsRating;

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
    public EventsRating addEventsDislike(Long userId, List<Long> eventIds) {
        return null;
    }

}