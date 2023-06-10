package ru.practicum.request.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.request.model.Request;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {



}