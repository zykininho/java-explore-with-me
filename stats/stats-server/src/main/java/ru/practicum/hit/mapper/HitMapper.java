package ru.practicum.hit.mapper;

import org.mapstruct.Mapping;
import ru.practicum.hit.dto.EndpointHitDto;
import ru.practicum.hit.model.EndpointHit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HitMapper {

    @Mapping(target = "timestamp", source = "timestamp", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EndpointHitDto toEndpointHitDto(EndpointHit endpointHit);

    @Mapping(target = "timestamp", source = "timestamp", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EndpointHit toEndpointHit(EndpointHitDto endpointHitDto);

}