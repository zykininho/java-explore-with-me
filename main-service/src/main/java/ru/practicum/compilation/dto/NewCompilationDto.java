package ru.practicum.compilation.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Data
@Builder
public class NewCompilationDto {

    private List<Long> events;
    private Boolean pinned;
    private String title;

}