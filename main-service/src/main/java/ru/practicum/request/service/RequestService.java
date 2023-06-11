package ru.practicum.request.service;

import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(long userId);

    ParticipationRequestDto createUserRequest(long userId, long eventId);

    ParticipationRequestDto cancelUserRequest(long userId, long requestId);

}