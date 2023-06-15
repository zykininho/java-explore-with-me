package ru.practicum.rating.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

@Data
@Builder
public class UserTopRating {

    private List<UserShortDto> users;

}