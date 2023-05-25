package hit.controller;

import hit.dto.EndpointHitDto;
import hit.service.HitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/hit")
public class HitController {

    private final HitService hitService;

    @PostMapping
    public ResponseEntity<EndpointHitDto> addNewHit(@RequestBody EndpointHitDto endpointHitDto) {
        log.info("Received POST-request at /hit endpoint with body={}", endpointHitDto);
        return ResponseEntity.ok().body(hitService.addNewHit(endpointHitDto));
    }

}