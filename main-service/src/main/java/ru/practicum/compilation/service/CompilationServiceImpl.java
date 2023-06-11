package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repo.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repo.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    @Autowired
    private CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(PageRequest.of(from, size)).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, PageRequest.of(from, size));
        }
        log.info("Найдены подборки событий {}", compilations);
        return compilations.stream()
                .map(compilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto searchCompilation(long compId) {
        Compilation foundCompilation = findCompilation(compId);
        log.info("Найдена подборка событий {}", foundCompilation);
        return compilationMapper.toCompilationDto(foundCompilation);
    }

    private Compilation findCompilation(long compId) {
        if (compId == 0) {
            throw new ValidationException();
        }
        Optional<Compilation> compilation = compilationRepository.findById(compId);
        if (compilation.isEmpty()) {
            throw new NotFoundException();
        }
        return compilation.get();
    }

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Добавлена новая подборка событий {}", savedCompilation);
        return compilationMapper.toCompilationDto(savedCompilation);
    }

    @Override
    public void deleteCompilation(long compId) {
        Compilation foundCompilation = findCompilation(compId);
        compilationRepository.delete(foundCompilation);
        log.info("Удалена подборка событий {}", foundCompilation);
    }

    @Override
    public CompilationDto updateCompilation(long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation foundCompilation = findCompilation(compId);
        if (updateCompilationRequest.getPinned() != null) {
            foundCompilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            foundCompilation.setTitle(updateCompilationRequest.getTitle());
        }
        List<Long> eventsId = updateCompilationRequest.getEvents();
        if (eventsId != null) {
            List<Event> events = new ArrayList<>();
            for (Long eventId : eventsId) {
                events.add(findEvent(eventId));
            }
            foundCompilation.setEvents(events);
        }
        Compilation updatedCompilation = compilationRepository.save(foundCompilation);
        log.info("Изменена подборка событий: {} vs. {}", foundCompilation, updatedCompilation);
        return compilationMapper.toCompilationDto(updatedCompilation);
    }

    private Event findEvent(long eventId) {
        if (eventId == 0) {
            throw new ValidationException();
        }
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new NotFoundException();
        }
        return event.get();
    }

}