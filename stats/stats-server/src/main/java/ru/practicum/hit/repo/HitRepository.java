package ru.practicum.hit.repo;

import org.springframework.stereotype.Repository;
import ru.practicum.hit.model.EndpointHit;
import org.springframework.data.jpa.repository.JpaRepository;


@Repository
public interface HitRepository extends JpaRepository<EndpointHit, Long> {



}