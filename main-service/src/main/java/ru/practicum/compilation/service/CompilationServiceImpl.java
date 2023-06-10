package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.repo.CompilationRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    @Autowired
    private CompilationMapper compilationMapper;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        // TODO: написать реализацию метода поиска подборок событий
        return null;
    }

    @Override
    public CompilationDto findCompilation(Long compId) {
        // TODO: написать реализацию метода поиска подборки событий
        return null;
    }

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        // TODO: написать реализацию метода создания подборки событий
        return null;
    }

    @Override
    public void deleteCompilation(Long compId) {
        // TODO: написать реализацию метода удаления подборки событий
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        // TODO: написать реализацию метода обновления подборки событий
        return null;
    }

}