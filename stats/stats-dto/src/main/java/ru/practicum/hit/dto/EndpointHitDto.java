package ru.practicum.hit.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EndpointHitDto {

    private String app;
    private String uri;
    private String ip;
    private String timestamp;

}