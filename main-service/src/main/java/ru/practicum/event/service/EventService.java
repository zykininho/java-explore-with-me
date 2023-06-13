package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> getAll(String text,
                               List<Long> categories,
                               Boolean paid,
                               LocalDateTime rangeStart,
                               LocalDateTime rangeEnd,
                               Boolean onlyAvailable,
                               String sort,
                               Integer from,
                               Integer size);

    EventFullDto find(long id);

    List<EventFullDto> getAdminAll(List<Long> users,
                                   List<String> states,
                                   List<Long> categories,
                                   LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd,
                                   int from,
                                   int size);

    EventFullDto updateAdmin(long eventId, UpdateEventAdminRequest updateRequest);

    List<EventShortDto> getUserEvents(long userId, int from, int size);

    EventFullDto createUserEvent(long userId, NewEventDto newEventDto);

    EventFullDto findUserEvent(long userId, long eventId);

    EventFullDto updateUserEvent(long userId, long eventId, UpdateEventUserRequest updateRequest);

    List<ParticipationRequestDto> findUserEventRequests(long userId, long eventId);

    EventRequestStatusUpdateResult updateUserEventRequests(long userId, long eventId, EventRequestStatusUpdateRequest updateRequest);

}