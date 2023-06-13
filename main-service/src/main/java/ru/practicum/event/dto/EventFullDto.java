package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.model.EventState;
import ru.practicum.location.dto.LocationDto;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
public class EventFullDto {

    private Long id;
    private String annotation;
    private CategoryDto category;
    private int confirmedRequests;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private UserShortDto initiator;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private Boolean requestModeration;
    private EventState state;
    private String title;
    private int views;

}