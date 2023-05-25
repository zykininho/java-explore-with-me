package hit.mapper;

import hit.dto.EndpointHitDto;
import hit.model.EndpointHit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {

    EndpointHitDto toEndpointHitDto(EndpointHit endpointHit);

    EndpointHit toEndpointHit(EndpointHitDto endpointHitDto);

}