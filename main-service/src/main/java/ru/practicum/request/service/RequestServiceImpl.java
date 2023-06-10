package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.repo.RequestRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        // TODO: написать реализацию метода поиска запросов на участие пользователя
        return null;
    }

    @Override
    public ParticipationRequestDto createUserRequest(Long userId, Long eventId) {
        // TODO: написать реализацию метода создания запроса на участие пользователя
        return null;
    }

    @Override
    public ParticipationRequestDto cancelUserRequest(Long userId, Long requestId) {
        // TODO: написать реализацию метода отмены запроса на участие пользователя
        return null;
    }

}