package ru.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.user.model.User;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "creationDate", source = "created")
    Request toRequest(ParticipationRequestDto participationRequestDto);

    @Mapping(target = "created", source = "creationDate")
    ParticipationRequestDto toParticipationRequestDto(Request request);

    default Event mapIdToEvent(Long eventId) {
        return Event.builder()
                        .id(eventId)
                        .build();
    }

    default Long mapEventToId(Event event) {
        return event.getId();
    }

    default User mapIdToUser(Long userId) {
        return User.builder()
                .id(userId)
                .build();
    }

    default Long mapUserToId(User user) {
        return user.getId();
    }

}