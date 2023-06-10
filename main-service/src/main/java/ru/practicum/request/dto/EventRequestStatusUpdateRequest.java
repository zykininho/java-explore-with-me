package ru.practicum.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.request.model.RequestStatus;

@Data
@Builder
public class EventRequestStatusUpdateRequest {

    private Long[] requestIds;
    private RequestStatus status;

}