package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(NewCategoryDto newCategoryDto);

    CategoryDto update(long catId, NewCategoryDto newCategoryDto);

    void delete(long catId);

    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto find(long catId);

}