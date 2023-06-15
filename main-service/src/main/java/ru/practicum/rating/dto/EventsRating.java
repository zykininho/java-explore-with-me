package ru.practicum.rating.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EventsRating {

    private List<Long> eventIds;
    private String rating;

}