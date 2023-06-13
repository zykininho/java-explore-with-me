package ru.practicum.request.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.Event;
import ru.practicum.request.model.Request;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequester(User requester);

    List<Request> findAllByEventAndRequester(Event event, User requester);

    Optional<Request> findByIdAndRequester(long id, User requester);

    List<Request> findAllByEvent(Event event);

    List<Request> findAllByIdInAndEvent(List<Long> requestsIdForUpdate, Event event);

}