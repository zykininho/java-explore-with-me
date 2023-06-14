package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "creationDate", source = "createdOn")
    @Mapping(target = "publishedDate", source = "publishedOn")
    Event toEvent(EventFullDto eventFullDto);

    @Mapping(target = "createdOn", source = "creationDate")
    @Mapping(target = "publishedOn", source = "publishedDate")
    EventFullDto toEventFullDto(Event event);

    Event toEvent(EventShortDto eventShortDto);

    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    Event toEvent(NewEventDto newEventDto);

    NewEventDto toNewEventDto(Event event);

    default Category mapIdToCategory(Long catId) {
        return Category.builder()
                .id(catId)
                .build();
    }

    default Long mapCategoryToId(Category category) {
        return category.getId();
    }

    default LocalDateTime mapToLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(localDateTime.format(formatter), formatter);
    }

}