package ru.practicum.hit.controller;

import ru.practicum.hit.dto.EndpointHitDto;
import ru.practicum.hit.service.HitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.ViewStatsDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping
public class HitController {

    private final HitService hitService;

    @PostMapping(value = "/hit")
    public ResponseEntity<EndpointHitDto> addNewHit(@RequestBody EndpointHitDto endpointHitDto) {
        log.info("Received POST-request at /hit endpoint with body={}", endpointHitDto);
        return ResponseEntity.ok().body(hitService.addNewHit(endpointHitDto));
    }

    @GetMapping(value = "/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(@RequestParam String start,
                                                       @RequestParam String end,
                                                       @RequestParam(required = false) List<String> uris,
                                                       @RequestParam(defaultValue = "false") String unique) {
        log.info("Received GET-request at /stats?start={}&end={}"
                + (uris != null ? "&uris=" + uris : "")
                + (unique != null ? "&unique=" + unique : "")
                + " endpoint", start, end);
        return ResponseEntity.ok().body(hitService.getStats(start, end, uris, unique));
    }

}