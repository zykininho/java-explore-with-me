package ru.practicum.compilation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UpdateCompilationRequest {

    private List<Long> events;
    private Boolean pinned;
    private String title;

}