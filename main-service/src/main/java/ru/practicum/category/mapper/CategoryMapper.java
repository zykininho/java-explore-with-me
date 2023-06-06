package ru.practicum.category.mapper;

import org.mapstruct.Mapper;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryDto categoryDto);

    CategoryDto toCategoryDto(Category category);

    Category toCategory(NewCategoryDto newCategoryDto);

    NewCategoryDto toNewCategoryDto(Category category);

}