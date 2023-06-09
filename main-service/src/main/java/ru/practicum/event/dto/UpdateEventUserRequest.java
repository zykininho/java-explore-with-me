package ru.practicum.event.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.location.dto.LocationDto;

import java.time.LocalDateTime;

@Data
@Builder
public class UpdateEventUserRequest {

    private String annotation;
    private long category;
    private String description;
    private LocalDateTime eventDate;
    private LocationDto location;
    private boolean paid;
    private int participantLimit;
    private boolean requestModeration;
    private String stateAction;
    private String title;

}