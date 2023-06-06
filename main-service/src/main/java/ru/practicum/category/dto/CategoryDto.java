package ru.practicum.category.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDto {

    private Long id;
    private String name;

}