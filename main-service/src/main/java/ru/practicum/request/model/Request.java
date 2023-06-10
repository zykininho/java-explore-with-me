package ru.practicum.request.model;

import lombok.*;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "requests", schema = "public")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Event event;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private User requester;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;
        return id != null && id.equals(((Request) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}