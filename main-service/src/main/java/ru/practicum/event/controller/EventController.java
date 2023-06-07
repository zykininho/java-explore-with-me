package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.service.EventService;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

}