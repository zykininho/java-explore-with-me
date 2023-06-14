package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryDto> create(@RequestBody(required = false) NewCategoryDto newCategoryDto) {
        log.info("Received POST-request at /admin/categories endpoint with body={}", newCategoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(newCategoryDto));
    }

    @PatchMapping("/admin/categories/{id}")
    public ResponseEntity<CategoryDto> update(@PathVariable("id") Long catId,
                                              @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Received PATCH-request at /admin/categories/{} endpoint with body {}", catId, newCategoryDto);
        return ResponseEntity.ok().body(categoryService.update(catId, newCategoryDto));
    }

    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<CategoryDto> delete(@PathVariable("id") Long catId) {
        log.info("Received DELETE-request at /admin/categories/{} endpoint", catId);
        categoryService.delete(catId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAll(@RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("Received GET-request at /categories?from={}&size={} endpoint", from, size);
        return ResponseEntity.ok().body(categoryService.getAll(from, size));
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDto> find(@PathVariable("id") Long catId) {
        log.info("Received GET-request at /categories/{} endpoint", catId);
        return ResponseEntity.ok().body(categoryService.find(catId));
    }

}