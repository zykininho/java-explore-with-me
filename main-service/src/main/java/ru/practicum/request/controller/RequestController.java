package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable Long userId) {
        log.info("Received GET-request at /users/{}/requests endpoint", userId);
        return ResponseEntity.ok().body(requestService.getUserRequests(userId));
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> createUserRequest(@PathVariable Long userId,
                                                                     @RequestParam(required = false) Long eventId) {
        log.info("Received POST-request at /users/{}/requests?eventId={} endpoint", userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.createUserRequest(userId, eventId));
    }

    @PatchMapping("{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelUserRequest(@PathVariable Long userId,
                                                                     @PathVariable Long requestId) {
        log.info("Received PATCH-request at /users/{}/requests/{}/cancel endpoint", userId, requestId);
        return ResponseEntity.ok().body(requestService.cancelUserRequest(userId, requestId));
    }

}