package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.client.StatClient;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.repo.EventRepository;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private UserMapper userMapper;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final StatClient statClient;

    @Override
    public List<EventShortDto> getAll(String text,
                                      List<Integer> categories,
                                      Boolean paid,
                                      LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd,
                                      Boolean onlyAvailable,
                                      String sort,
                                      Integer from,
                                      Integer size) {
        log.info("Extracting all events");
        validateSearchParameters(from, size, sort);
        String sql = "SELECT * " +
                "FROM public.events AS events" +
                "LEFT JOIN (SELECT req.event_id AS event, COUNT(req.id) AS amount" +
                "FROM public.requests as req" +
                "GROUP BY req.event_id" +
                "WHERE req.status = 'CONFIRMED') AS requests ON (events.id = requests.event_id " +
                "AND events.participant_limit >= requests.amount)" +
                "WHERE state = 'PUBLISHED'";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        if (text != null && !text.isBlank()) {
            sql += " AND ((UPPER(events.annotation) LIKE UPPER(CONCAT('%', :text, '%')))" +
                      "OR (UPPER(events.description) LIKE UPPER(CONCAT('%', :text, '%'))))";
            parameters.addValue("text", text);
        }
        if (categories != null && !categories.isEmpty()) {
            sql += " AND (events.category IN (:categories))";
            parameters.addValue("categories", categories);
        }
        if (paid != null) {
            sql += " AND (events.paid = :paid)";
            parameters.addValue("paid", paid);
        }
        if (rangeStart != null && rangeEnd != null) {
            sql += " AND (events.event_date >= :start AND events.event_date <= :end)";
            parameters.addValue("start", rangeStart);
            parameters.addValue("end", rangeEnd);
        } else {
            sql += " AND (events.event_date >= :now)";
            parameters.addValue("now", LocalDateTime.now());
        }
        if (onlyAvailable != null && onlyAvailable) {
            sql += " AND (requests.amount IS NOT NULL)";
        }
        List<EventShortDto> events = namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> createEventShort(rs));
        if (sort.equals("EVENT_DATE")) {
            events = events.stream()
                    .sorted(Comparator.comparing(EventShortDto::getEventDate))
                    .collect(Collectors.toList());
        } else if (sort.equals("VIEWS")) {
            events = events.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews))
                    .collect(Collectors.toList());
        }
        return events;
    }

    private void validateSearchParameters(int from, int size, String sort) {
        if (from < 0) {
            log.info("Параметр запроса 'from' должен быть больше или равен 0, указано значение {}", from);
            throw new ValidationException();
        } else if (size <= 0) {
            log.info("Параметр запроса 'size' должен быть больше 0, указано значение {}", size);
            throw new ValidationException();
        } else if (sort == null || sort.isBlank()) {
            log.info("Параметр запроса 'sort' должен принимать значение сортировки");
            throw new ValidationException();
        }
    }

    private EventShortDto createEventShort(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String[] uris = {"/event/" + id};
        LocalDateTime eventDate = LocalDateTime.parse(rs.getString("event_date"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        ResponseEntity<Object> stats = statClient.getStats(eventDate, eventDate, uris, false);
        int views = 0;
        if (stats.hasBody()) {
            ViewStatsDto body = (ViewStatsDto) stats.getBody();
            views = body.getHits();
        }
        return EventShortDto.builder()
                .id(id)
                .annotation(rs.getString("annotation"))
                .category(categoryMapper.toCategoryDto(rs.getObject("category", Category.class)))
                .confirmedRequests(rs.getInt("amount"))
                .eventDate(eventDate)
                .initiator(userMapper.toUserShortDto(rs.getObject("initiator", User.class)))
                .paid(rs.getBoolean("paid"))
                .title(rs.getString("title"))
                .views(views)
                .build();
    }

    @Override
    public EventFullDto find(long id) {
        // TODO: написать реализацию метода поиска события
        return null;
    }

    @Override
    public List<EventFullDto> getAdminAll(List<Integer> users, List<String> states, List<Integer> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        // TODO: написать реализацию метода поиска событий администратором
        return null;
    }

    @Override
    public EventFullDto updateAdmin(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        // TODO: написать реализацию метода обновления события администратором
        return null;
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        // TODO: написать реализацию метода поиска событий пользователя
        return null;
    }

    @Override
    public EventFullDto createUserEvents(Long userId, NewEventDto newEventDto) {
        // TODO: написать реализацию метода создания события пользователя
        return null;
    }

    @Override
    public EventFullDto findUserEvent(Long userId, Long eventId) {
        // TODO: написать реализацию метода поиска события пользователя
        return null;
    }

    @Override
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        // TODO: написать реализацию метода обновления события пользователя
        return null;
    }

    @Override
    public List<ParticipationRequestDto> findUserEventRequests(Long userId, Long eventId) {
        // TODO: написать реализацию метода поиска запросов на участие в событии пользователя
        return null;
    }

    @Override
    public EventRequestStatusUpdateResult updateUserEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        // TODO: написать реализацию метода обновления запросов на участие в событии пользователя
        return null;
    }

}