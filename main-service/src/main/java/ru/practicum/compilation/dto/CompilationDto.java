package ru.practicum.compilation.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Data
@Builder
public class CompilationDto {

    private Long id;
    private List<EventShortDto> events;
    private Boolean pinned;
    private String title;

}