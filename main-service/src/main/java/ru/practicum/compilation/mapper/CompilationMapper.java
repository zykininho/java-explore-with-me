package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.model.Event;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    Compilation toCompilation(CompilationDto compilationDto);

    CompilationDto toCompilationDto(Compilation compilation);

    Compilation toCompilation(NewCompilationDto newCompilationDto);

    NewCompilationDto toNewCompilationDto(Compilation compilation);

    default List<Event> mapIdToEvent(List<Long> eventsId) {
        List<Event> events = new ArrayList<>();
        if (eventsId != null) {
            for (Long eventId : eventsId) {
                Event event = Event.builder()
                        .id(eventId)
                        .build();
                events.add(event);
            }
        }
        return events;
    }

    default List<Long> mapEventToId(List<Event> events) {
        List<Long> eventsId = new ArrayList<>();
        for (Event event : events) {
            eventsId.add(event.getId());
        }
        return eventsId;
    }

}