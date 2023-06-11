package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repo.CategoryRepository;
import ru.practicum.client.StatClient;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repo.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.location.mapper.LocationMapper;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LocationMapper locationMapper;
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

    private void validateSearchParameters(int from, int size) {
        if (from < 0) {
            log.info("Параметр запроса 'from' должен быть больше или равен 0, указано значение {}", from);
            throw new ValidationException();
        } else if (size <= 0) {
            log.info("Параметр запроса 'size' должен быть больше 0, указано значение {}", size);
            throw new ValidationException();
        }
    }

    private EventShortDto createEventShort(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        LocalDateTime eventDate = LocalDateTime.parse(rs.getString("event_date"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return EventShortDto.builder()
                .id(id)
                .annotation(rs.getString("annotation"))
                .category(categoryMapper.toCategoryDto(rs.getObject("category", Category.class)))
                .confirmedRequests(rs.getInt("amount"))
                .eventDate(eventDate)
                .initiator(userMapper.toUserShortDto(rs.getObject("initiator", User.class)))
                .paid(rs.getBoolean("paid"))
                .title(rs.getString("title"))
                .views(getNumberViews(id, eventDate))
                .build();
    }

    @Override
    public EventFullDto find(long id) {
        Event event = findEvent(id);
        if (event.getState() != EventState.PUBLISHED) {
            log.info("Событие {} не опубликовано", event);
            throw new NotFoundException();
        }
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        eventFullDto.setViews(getNumberViews(id, event.getEventDate()));
        eventFullDto.setConfirmedRequests(getNumberConfirmedRequests(event));
        log.info("Найдено событие {}", eventFullDto);
        return eventFullDto;
    }

    private Event findEvent(long eventId) {
        if (eventId == 0) {
            throw new ValidationException();
        }
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new NotFoundException();
        }
        return event.get();
    }

    @Override
    public List<EventFullDto> getAdminAll(List<Integer> users,
                                          List<String> states,
                                          List<Integer> categories,
                                          LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd,
                                          Integer from,
                                          Integer size) {
        validateSearchParameters(from, size);
        List<EventFullDto> eventsFullDto = new ArrayList<>();
        List<Event> events;
        if (users == null) {
            events = eventRepository.findAll(PageRequest.of(from, size)).getContent();
        } else {
            events = eventRepository.findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateIsAfterAndEventDateIsBefore(
                    users,
                    states,
                    categories,
                    rangeStart,
                    rangeEnd,
                    PageRequest.of(from, size));
        }
        log.info("Получены события {}", events);
        for (Event event : events) {
            EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
            eventFullDto.setViews(getNumberViews(event.getId(), event.getEventDate()));
            eventFullDto.setConfirmedRequests(getNumberConfirmedRequests(event));
            eventsFullDto.add(eventFullDto);
        }
        log.info("Найдены представления событий {}", eventsFullDto);
        return eventsFullDto;
    }

    private int getNumberViews(long eventId, LocalDateTime eventDate) {
        String[] uris = {"/event/" + eventId};
        ResponseEntity<Object> stats = statClient.getStats(eventDate, eventDate, uris, false);
        int views = 0;
        if (stats.hasBody()) {
            ViewStatsDto body = (ViewStatsDto) stats.getBody();
            views = body.getHits();
        }
        return views;
    }

    private int getNumberConfirmedRequests(Event event) {
        return (int) event.getRequests().stream()
                .filter(request -> request.getStatus().equals(RequestStatus.CONFIRMED))
                .count();
    }

    @Override
    public EventFullDto updateAdmin(long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = findEvent(eventId);
        String stateAction = updateRequest.getStateAction();
        if (stateAction != null) {
            switch (stateAction) {
                case "PUBLISH_EVENT":
                    if (!event.getState().equals(EventState.PENDING)) {
                        log.info("Событие можно публиковать, только если оно в состоянии ожидания публикации." +
                                        "Текущий статус события - {}",
                                event.getState());
                        throw new ConflictException();
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedDate(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
                    if (!event.getState().equals(EventState.PENDING)) {
                        log.info("Событие можно отклонить, только если оно еще не опубликовано." +
                                        "Текущий статус события - {}",
                                event.getState());
                        throw new ConflictException();
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
        LocalDateTime newEventDate = updateRequest.getEventDate();
        if (newEventDate != null && event.getState().equals(EventState.PUBLISHED)) {
            LocalDateTime publishedDate = event.getPublishedDate();
            long diff = ChronoUnit.HOURS.between(newEventDate, publishedDate);
            if (diff <= 1) {
                log.info("Дата начала {} изменяемого события должна быть не ранее, чем за час от даты публикации {}",
                        newEventDate,
                        publishedDate);
                throw new ConflictException();
            }
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(event.getParticipantLimit());
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(updateRequest.getLocation()));
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getCategory() != null) {
            event.setCategory(findCategory(updateRequest.getCategory()));
        }
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        Event updatedEvent = eventRepository.save(event);
        log.info("Обновлено событие {} на основании запроса администратора {}", updatedEvent, updateRequest);
        return eventMapper.toEventFullDto(updatedEvent);
    }

    private Category findCategory(long catId) {
        if (catId == 0) {
            throw new ValidationException();
        }
        Optional<Category> category = categoryRepository.findById(catId);
        if (category.isEmpty()) {
            throw new NotFoundException();
        }
        return category.get();
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