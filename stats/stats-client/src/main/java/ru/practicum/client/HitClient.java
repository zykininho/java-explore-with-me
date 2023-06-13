package ru.practicum.client;

import ru.practicum.hit.dto.EndpointHitDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Service
public class HitClient extends BaseClient {

    private static final String API_HIT_PREFIX = "/hit";

    @Autowired
    public HitClient(@Value("${ewm-stats.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_HIT_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> postHit(EndpointHitDto endpointHitDto) {
        return post("", null, endpointHitDto);
    }

}