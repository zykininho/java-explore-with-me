package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repo.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repo.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(long userId) {
        User requester = findUser(userId);
        List<Request> userRequests = requestRepository.findAllByRequester(requester);
        if (userRequests.isEmpty()) {
            log.info("Не найдены запросы на участие в событиях от пользователя {}", requester);
            return new ArrayList<>();
        }
        log.info("Найдены запросы на участие в событиях {} от пользователя {}", userRequests, requester);
        return userRequests.stream()
                .map(requestMapper::toParticipationRequestDto)
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
    public ParticipationRequestDto createUserRequest(long userId, Long eventId) {
        if (eventId == null) {
            log.info("Не указан обязательный параметр запроса eventId");
            throw new ValidationException();
        }
        User requester = findUser(userId);
        Event event = findEvent(eventId);
        List<Request> userEventRequests = requestRepository.findAllByEventAndRequester(event, requester);
        if (!userEventRequests.isEmpty()) {
            log.info("Найдены повторные запросы на участие в событии {} от пользователя {}", event, requester);
            throw new ConflictException();
        }
        if (event.getInitiator() == requester) {
            log.info("Инициатор события {} не может добавить запрос на участие в своём событии {}", requester, event);
            throw new ConflictException();
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.info("Нельзя участвовать в неопубликованном событии {}", event);
            throw new ConflictException();
        }
        if (eventParticipantLimitReached(event)) {
            log.info("У события {} достигнут лимит запросов на участие", event);
            throw new ConflictException();
        }
        Request newRequest = Request.builder()
                .event(event)
                .status(Boolean.TRUE.equals(event.getRequestModeration()) ? RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .requester(requester)
                .creationDate(LocalDateTime.now())
                .build();
        if (event.getParticipantLimit() == 0) {
            newRequest.setStatus(RequestStatus.CONFIRMED);
        }
        Request savedRequest = requestRepository.save(newRequest);
        log.info("В базе сохранен новый запрос на участие в событии {}", savedRequest);
        return requestMapper.toParticipationRequestDto(savedRequest);
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

    private boolean eventParticipantLimitReached(Event event) {
        long numberConfirmedRequests = event.getRequests().stream()
                .filter(request -> request.getStatus().equals(RequestStatus.CONFIRMED))
                .count();
        return numberConfirmedRequests != 0 && numberConfirmedRequests >= event.getParticipantLimit();
    }

    @Override
    public ParticipationRequestDto cancelUserRequest(long userId, long requestId) {
        User requester = findUser(userId);
        Request request = findRequest(requestId, requester);
        request.setStatus(RequestStatus.CANCELED);
        Request cancelledRequest = requestRepository.save(request);
        log.info("В базе отменено событие {} пользователем {}", cancelledRequest, requester);
        return requestMapper.toParticipationRequestDto(cancelledRequest);
    }

    private Request findRequest(long requestId, User requester) {
        if (requestId == 0) {
            throw new ValidationException();
        }
        Optional<Request> request = requestRepository.findByIdAndRequester(requestId, requester);
        if (request.isEmpty()) {
            throw new NotFoundException();
        }
        return request.get();
    }

}