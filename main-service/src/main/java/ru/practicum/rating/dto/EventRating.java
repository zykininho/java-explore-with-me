package ru.practicum.rating.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventRating {

    private Long eventIds;
    private String rating;

}