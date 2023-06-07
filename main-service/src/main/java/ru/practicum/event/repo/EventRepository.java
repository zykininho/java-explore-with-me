package ru.practicum.event.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {



}