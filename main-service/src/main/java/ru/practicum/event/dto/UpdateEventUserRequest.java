package ru.practicum.event.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.location.dto.LocationDto;

@Data
@Builder
public class UpdateEventUserRequest {

    private String annotation;
    private Long category;
    private String description;
    private String eventDate;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String stateAction;
    private String title;

}