package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repo.CategoryRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        Category category = categoryMapper.toCategory(newCategoryDto);
        validate(category);
        Category savedCategory = categoryRepository.save(category);
        log.info("Добавлена новая категория: {}", savedCategory);
        return categoryMapper.toCategoryDto(savedCategory);
    }

    private void validate(Category category) {
        String name = category.getName();
        if (name == null || name.isBlank()) {
            log.info("У категории {} указано неверное наименование", category);
            throw new ValidationException();
        }
        if (name.length() > 50) {
            log.info("У категории {} длина наименования больше 50 символов", category);
            throw new ValidationException();
        }
    }

    @Override
    public CategoryDto update(long catId, NewCategoryDto newCategoryDto) {
        Category category = findCategory(catId);
        validateToUpdate(newCategoryDto);
        category.setName(newCategoryDto.getName());
        Category savedCategory = categoryRepository.save(category);
        log.info("Изменена категория {}", savedCategory);
        return categoryMapper.toCategoryDto(savedCategory);
    }

    private Category findCategory(long catId) {
        if (catId == 0) {
            throw new ValidationException();
        }
        Optional<Category> category = categoryRepository.findById(catId);
        if (category.isEmpty()) {
            throw new NotFoundException();
        }
        return category.get();
    }

    private void validateToUpdate(NewCategoryDto newCategoryDto) {
        String name = newCategoryDto.getName();
        if (name == null || name.isBlank()) {
            log.info("У категории {} указано неверное наименование", newCategoryDto);
            throw new ValidationException();
        }
        if (name.length() > 50) {
            log.info("У категории {} длина наименования больше 50 символов", newCategoryDto);
            throw new ValidationException();
        }
    }

    @Override
    public void delete(long catId) {
        Category category = findCategory(catId);
        categoryRepository.delete(category);
        log.info("Удалена категория {}", category);
    }

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {
        validateSearchParameters(from, size);
        List<Category> categories = categoryRepository.findAll(PageRequest.of(from / size, size)).toList();
        return categories.stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    private void validateSearchParameters(int from, int size) {
        if (from < 0) {
            log.info("Параметр запроса 'from' должен быть больше или равен 0, указано значение {}", from);
            throw new ValidationException();
        } else if (size <= 0) {
            log.info("Параметр запроса 'size' должен быть больше 0, указано значение {}", size);
            throw new ValidationException();
        }
    }

    @Override
    public CategoryDto find(long catId) {
        Category category = findCategory(catId);
        log.info("Найдена категория {}", category);
        return categoryMapper.toCategoryDto(category);
    }

}