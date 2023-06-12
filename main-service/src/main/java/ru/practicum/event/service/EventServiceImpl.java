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
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repo.RequestRepository;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repo.UserRepository;

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
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LocationMapper locationMapper;
    @Autowired
    private RequestMapper requestMapper;
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
            if (!event.getState().equals(EventState.PENDING)) {
                log.info("Событие можно публиковать или отклонить, только если оно в состоянии ожидания публикации." +
                                "Текущий статус события - {}",
                        event.getState());
                throw new ConflictException();
            }
            switch (stateAction) {
                case "PUBLISH_EVENT":
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedDate(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
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
    public List<EventShortDto> getUserEvents(long userId, int from, int size) {
        User initiator = findUser(userId);
        List<Event> userEvents = eventRepository.findAllByInitiator(initiator, PageRequest.of(from, size));
        log.info("Найдены события {} пользователя {}", userEvents, initiator);
        return userEvents.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    private User findUser(long userId) {
        if (userId == 0) {
            throw new ValidationException();
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException();
        }
        return user.get();
    }

    @Override
    public EventFullDto createUserEvent(long userId, NewEventDto newEventDto) {
        User initiator = findUser(userId);
        Event newEvent = eventMapper.toEvent(newEventDto);
        newEvent.setInitiator(initiator);
        newEvent.setState(EventState.PENDING);
        LocalDateTime eventDate = newEvent.getEventDate();
        if (eventDate != null) {
            long diff = ChronoUnit.HOURS.between(eventDate, LocalDateTime.now());
            if (diff <= 2) {
                log.info("Дата и время {}, на которые намечено событие {}, не может быть раньше, чем через 2 часа от текущего момента",
                        eventDate,
                        newEvent);
                throw new ConflictException();
            }
        }
        Event savedEvent = eventRepository.save(newEvent);
        log.info("Добавлено новое событие {} от пользователя {}", savedEvent, initiator);
        return eventMapper.toEventFullDto(savedEvent);
    }

    @Override
    public EventFullDto findUserEvent(long userId, long eventId) {
        User initiator = findUser(userId);
        Event foundEvent = findEvent(eventId, initiator);
        log.info("Найдено событие {} от пользователя {}", foundEvent, initiator);
        return eventMapper.toEventFullDto(foundEvent);
    }

    private Event findEvent(long eventId, User initiator) {
        if (eventId == 0) {
            throw new ValidationException();
        }
        Optional<Event> event = eventRepository.findByIdAndInitiator(eventId, initiator);
        if (event.isEmpty()) {
            throw new NotFoundException();
        }
        return event.get();
    }

    @Override
    public EventFullDto updateUserEvent(long userId, long eventId, UpdateEventUserRequest updateRequest) {
        User initiator = findUser(userId);
        Event event = findEvent(eventId, initiator);
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            event.setCategory(findCategory(updateRequest.getCategory()));
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        LocalDateTime newEventDate = updateRequest.getEventDate();
        if (newEventDate != null && event.getState().equals(EventState.PUBLISHED)) {
            long diff = ChronoUnit.HOURS.between(newEventDate, LocalDateTime.now());
            if (diff <= 2) {
                log.info("дата и время {}, на которые намечено событие, не может быть раньше, чем через два часа от текущего момента {}",
                        newEventDate,
                        LocalDateTime.now());
                throw new ConflictException();
            }
        }
        if (newEventDate != null) {
            event.setEventDate(newEventDate);
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(updateRequest.getLocation()));
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(event.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
        String stateAction = updateRequest.getStateAction();
        if (stateAction != null) {
            if (event.getState().equals(EventState.PUBLISHED)) {
                log.info("Изменить можно только отмененные события или события в состоянии ожидания модерации." +
                                "Текущий статус - {}",
                        event.getState());
                throw new ConflictException();
            }
            switch (stateAction) {
                case "SEND_TO_REVIEW":
                    event.setState(EventState.PENDING);
                    break;
                case "CANCEL_REVIEW":
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
        Event updatedEvent = eventRepository.save(event);
        log.info("Обновлено событие {} на основании запроса пользователя {}", updatedEvent, updateRequest);
        return eventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    public List<ParticipationRequestDto> findUserEventRequests(long userId, long eventId) {
        User initiator = findUser(userId);
        Event event = findEvent(eventId, initiator);
        List<Request> requests = requestRepository.findAllByEvent(event);
        log.info("Найдены запросы {} на участие в событии {} пользователя {}", requests, event, initiator);
        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateUserEventRequests(long userId, long eventId, EventRequestStatusUpdateRequest updateRequest) {
        User initiator = findUser(userId);
        Event event = findEvent(eventId, initiator);
        RequestStatus updateStatus = updateRequest.getStatus();
        if (Boolean.FALSE.equals(event.getRequestModeration()) && updateStatus.equals(RequestStatus.CONFIRMED)) {
            return EventRequestStatusUpdateResult.builder().build();
        }
        if (getNumberConfirmedRequests(event) == event.getParticipantLimit()) {
            log.info("Нельзя подтвердить заявки, если уже достигнут лимит по заявкам на событие {}", event);
            throw new ConflictException();
        }
        Long[] requestsIdForUpdate = updateRequest.getRequestsId();
        List<Request> requests = requestRepository.findAllByIdAndEvent(requestsIdForUpdate, event);
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        boolean isLimitReached = false;
        for (Request request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                log.info("Статус можно изменить только у заявки, находящейся в состоянии ожидания." +
                        "Текущий статус - {}", request.getStatus());
                throw new ConflictException();
            }
            if (isLimitReached) {
                updateStatus = RequestStatus.REJECTED;
            }
            request.setStatus(updateStatus);
            ParticipationRequestDto participationRequestDto = requestMapper.toParticipationRequestDto(request);
            if (updateStatus.equals(RequestStatus.CONFIRMED)) {
                confirmedRequests.add(participationRequestDto);
                log.info("Заявка {} на участие в событии {} подтверждена", request, event);
            } else if (updateStatus.equals(RequestStatus.REJECTED)) {
                rejectedRequests.add(participationRequestDto);
                log.info("Заявка {} на участие в событии {} отменена", request, event);
            }
            if (!isLimitReached && (getNumberConfirmedRequests(event) == event.getParticipantLimit())) {
                isLimitReached = true;
                log.info("Достигнут лимит заявок по участию в событии {}", event);
            }
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

}