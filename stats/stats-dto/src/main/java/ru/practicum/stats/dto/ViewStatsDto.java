package ru.practicum.stats.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ViewStatsDto {

    private String app;
    private String uri;
    private Integer hits;

}