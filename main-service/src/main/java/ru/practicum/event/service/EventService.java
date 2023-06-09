package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> getAll(String text,
                               List<Integer> categories,
                               Boolean paid,
                               LocalDateTime rangeStart,
                               LocalDateTime rangeEnd,
                               Boolean onlyAvailable,
                               String sort,
                               Integer from,
                               Integer size);

    EventFullDto find(long id);

    List<EventFullDto> getAdminAll(List<Integer> users,
                                   List<String> states,
                                   List<Integer> categories,
                                   LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd,
                                   Integer from,
                                   Integer size);

    EventFullDto updateAdmin(long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto createUserEvents(Long userId, NewEventDto newEventDto);

    EventFullDto findUserEvent(Long userId, Long eventId);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> findUserEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateUserEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

}