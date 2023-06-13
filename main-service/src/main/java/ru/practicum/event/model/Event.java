package ru.practicum.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.category.model.Category;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.location.model.Location;
import ru.practicum.request.model.Request;
import ru.practicum.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "events", schema = "public")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String annotation;

    @Column
    private Boolean paid;

    @Column
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Category category;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Column
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private User initiator;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Enumerated(EnumType.STRING)
    private EventState state;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "events")
    private List<Compilation> compilations;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Request> requests;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        return id != null && id.equals(((Event) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}