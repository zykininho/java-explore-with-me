package ru.practicum.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.request.model.RequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class ParticipationRequestDto {

    private Long id;
    private Long event;
    private LocalDateTime creationDate;
    private Long requester;
    private RequestStatus status;

}