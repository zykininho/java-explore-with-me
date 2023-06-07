package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {

    Event toEvent(EventFullDto eventFullDto);

    EventFullDto toEventFullDto(Event event);

    Event toEvent(EventShortDto eventShortDto);

    EventShortDto toEventShortDto(Event event);

    Event toEvent(NewEventDto newEventDto);

    NewEventDto toNewEventDto(Event event);

}