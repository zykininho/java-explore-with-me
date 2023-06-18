package ru.practicum.rating.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Data
@Builder
public class EventTopRating {

    private List<EventShortDto> events;

}