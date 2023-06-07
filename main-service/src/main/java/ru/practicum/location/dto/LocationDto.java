package ru.practicum.location.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationDto {

    private float latitude;
    private float longitude;

}