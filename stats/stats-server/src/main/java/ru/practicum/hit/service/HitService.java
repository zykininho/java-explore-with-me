package ru.practicum.hit.service;

import ru.practicum.hit.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.util.List;

public interface HitService {

    EndpointHitDto addNewHit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStats(String start, String end, List<String> uris, String unique);

}