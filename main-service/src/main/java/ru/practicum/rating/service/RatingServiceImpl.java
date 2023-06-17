package ru.practicum.rating.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repo.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.rating.dto.EventRating;
import ru.practicum.rating.dto.EventTopRating;
import ru.practicum.rating.dto.EventsRating;
import ru.practicum.rating.dto.UserTopRating;
import ru.practicum.request.model.Request;
import ru.practicum.request.repo.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repo.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public EventsRating addEventsLike(Long userId, List<Long> eventIds) {
        User user = findUser(userId);
        List<Event> eventsToAddLike = new ArrayList<>();
        for (Long eventId : eventIds) {
            Event event = findEvent(eventId);
            checkUserTookPartInEvent(user, event);
            eventsToAddLike.add(event);
        }
        addLikes(userId, eventIds);
        return EventsRating.builder()
                .eventIds(eventsToAddLike.stream()
                        .map(eventMapper::toEventShortDto)
                        .collect(Collectors.toList()))
                .rating("like")
                .build();
    }

    private void addLikes(Long userId, List<Long> eventIds) {
        List<String> listValues = new ArrayList<>();
        String value = "(%d, :userId, 1)";
        StringBuilder sql = new StringBuilder("INSERT INTO RATINGS (EVENT_ID, USER_ID, RATING) VALUES ");
        for (Long eventId : eventIds) {
            listValues.add(String.format(value, eventId));
        }
        String values = String.join(",", listValues);
        sql.append(values);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userId", userId);
        namedJdbcTemplate.update(sql.toString(), parameters);
        log.info("Добавлен лайк событиям {} от пользователя {}", eventIds, userId);
    }

    @Override
    public EventRating addEventLike(Long userId, Long eventId) {
        User user = findUser(userId);
        Event event = findEvent(eventId);
        checkUserTookPartInEvent(user, event);
        int rating = getUserEventRating(userId, eventId);
        switch (rating) {
            case -1:
                deleteUserRatings(userId, List.of(eventId));
            case 0:
                addLikes(userId, List.of(eventId));
                break;
            case 1:
            log.info("Пользователь {} уже поставил лайк событию {}", user, event);
            throw new ConflictException();
        }
        return EventRating.builder()
                .event(eventMapper.toEventShortDto(event))
                .rating("like")
                .build();
    }

    private void checkUserTookPartInEvent(User user, Event event) {
        List<Request> userEventRequests = requestRepository.findAllByEventAndRequester(event, user);
        if (userEventRequests.isEmpty()) {
            log.info("Пользователь {} не принимал участие в событии {}", user, event);
            throw new ConflictException();
        }
    }

    private int getUserEventRating(long userId, long eventId) {
        String sql = "SELECT CASE\n" +
                "\t\tWHEN COUNT(RATING) = 0 THEN 0\n" +
                "\t\tWHEN SUM(RATING) = 1 THEN 1\n" +
                "\t\tWHEN SUM(RATING) = -1 THEN -1\n" +
                "\t   END\n" +
                "FROM PUBLIC.RATINGS\n" +
                "WHERE EVENT_ID = :eventId\n" +
                "\tAND USER_ID = :userId";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userId", userId);
        parameters.addValue("eventId", eventId);
        Integer rating = namedJdbcTemplate.queryForObject(sql, parameters, Integer.class);
        if (rating == null) {
            log.info("Не удалось получить рейтинг события {} от пользователя {}", userId, eventId);
            throw new ConflictException();
        }
        return rating;
    }

    private void deleteUserRatings(long userId, List<Long> eventIds) {
        String sql = "DELETE FROM RATINGS WHERE EVENT_ID IN (:eventIds) AND USER_ID = :userId";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("eventIds", eventIds);
        parameters.addValue("userId", userId);
        namedJdbcTemplate.update(sql, parameters);
        log.info("Удалены оценки у событий {} от пользователя {}", eventIds, userId);
    }

    @Override
    public EventsRating addEventsDislike(Long userId, List<Long> eventIds) {
        User user = findUser(userId);
        List<Event> eventsToAddLike = new ArrayList<>();
        for (Long eventId : eventIds) {
            Event event = findEvent(eventId);
            checkUserTookPartInEvent(user, event);
            eventsToAddLike.add(event);
        }
        addDislikes(userId, eventIds);
        return EventsRating.builder()
                .eventIds(eventsToAddLike.stream()
                        .map(eventMapper::toEventShortDto)
                        .collect(Collectors.toList()))
                .rating("dislike")
                .build();
    }

    private void addDislikes(Long userId, List<Long> eventIds) {
        List<String> listValues = new ArrayList<>();
        String value = "(%d, :userId, -1)";
        StringBuilder sql = new StringBuilder("INSERT INTO RATINGS (EVENT_ID, USER_ID, RATING) VALUES ");
        for (Long eventId : eventIds) {
            listValues.add(String.format(value, eventId));
        }
        String values = String.join(",", listValues);
        sql.append(values);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userId", userId);
        namedJdbcTemplate.update(sql.toString(), parameters);
        log.info("Добавлен дизлайк событиям {} от пользователя {}", eventIds, userId);
    }

    @Override
    public EventRating addEventDislike(Long userId, Long eventId) {
        User user = findUser(userId);
        Event event = findEvent(eventId);
        checkUserTookPartInEvent(user, event);
        int rating = getUserEventRating(userId, eventId);
        switch (rating) {
            case 1:
                deleteUserRatings(userId, List.of(eventId));
            case 0:
                addDislike(userId, eventId);
                break;
            case -1:
                log.info("Пользователь {} уже поставил дизлайк событию {}", user, event);
                throw new ConflictException();
        }
        return EventRating.builder()
                .event(eventMapper.toEventShortDto(event))
                .rating("dislike")
                .build();
    }

    private void addDislike(long userId, long eventId) {
        String sql = "INSERT INTO RATINGS (EVENT_ID, USER_ID, RATING)\n" +
                "VALUES (:eventId, :userId, -1)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userId", userId);
        parameters.addValue("eventId", eventId);
        namedJdbcTemplate.update(sql, parameters);
        log.info("Добавлен дизлайк событию {} от пользователя {}", eventId, userId);
    }

    @Override
    public void deleteEventsLike(Long userId, List<Long> eventIds) {
        User user = findUser(userId);
        for (Long eventId : eventIds) {
            Event event = findEvent(eventId);
            checkUserTookPartInEvent(user, event);
            int rating = getUserEventRating(userId, eventId);
            if (rating != 1) {
                log.info("Пользователь {} не ставил лайк событию {}", user, event);
                throw new ConflictException();
            }
        }
        deleteUserRatings(userId, eventIds);
    }

    @Override
    public void deleteEventLike(Long userId, Long eventId) {
        User user = findUser(userId);
        Event event = findEvent(eventId);
        int rating = getUserEventRating(userId, eventId);
        if (rating != 1) {
            log.info("Пользователь {} не ставил лайк событию {}", user, event);
            throw new ConflictException();
        }
        deleteUserRatings(userId, List.of(eventId));
    }

    @Override
    public void deleteEventsDislike(Long userId, List<Long> eventIds) {
        User user = findUser(userId);
        for (Long eventId : eventIds) {
            Event event = findEvent(eventId);
            checkUserTookPartInEvent(user, event);
            int rating = getUserEventRating(userId, eventId);
            if (rating != -1) {
                log.info("Пользователь {} не ставил дизлайк событию {}", user, event);
                throw new ConflictException();
            }
        }
        deleteUserRatings(userId, eventIds);
    }

    @Override
    public void deleteEventDislike(Long userId, Long eventId) {
        User user = findUser(userId);
        Event event = findEvent(eventId);
        int rating = getUserEventRating(userId, eventId);
        if (rating != -1) {
            log.info("Пользователь {} не ставил дизлайк событию {}", user, event);
            throw new ConflictException();
        }
        deleteUserRatings(userId, List.of(eventId));
    }

    @Override
    public EventTopRating getEventRating(Integer top) {
        // TODO: добавить реализацию
        return null;
    }

    @Override
    public UserTopRating getUserRating(Integer top) {
        // TODO: добавить реализацию
        return null;
    }

    private User findUser(long userId) {
        if (userId == 0) {
            log.info("Передан неверный идентификатор пользователя {}", userId);
            throw new ValidationException();
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.info("Не найден пользователь с идентификатором {}", userId);
            throw new NotFoundException();
        }
        log.info("Найден пользователь с идентификатором {}", userId);
        return user.get();
    }

    private Event findEvent(long eventId) {
        if (eventId == 0) {
            log.info("Передан неверный идентификатор события {}", eventId);
            throw new ValidationException();
        }
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            log.info("Не найдено событие с идентификатором {}", eventId);
            throw new NotFoundException();
        }
        log.info("Найдено событие с идентификатором {}", eventId);
        return event.get();
    }

}