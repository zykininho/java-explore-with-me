package ru.practicum.rating.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.event.dto.EventShortDto;

@Data
@Builder
public class EventRating {

    private EventShortDto event;
    private String rating;

}