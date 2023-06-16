package ru.practicum.rating.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Data
@Builder
public class EventsRating {

    private List<EventShortDto> eventIds;
    private String rating;

}