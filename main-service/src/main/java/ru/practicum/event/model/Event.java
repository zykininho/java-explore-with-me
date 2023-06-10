package ru.practicum.event.model;

import lombok.*;
import ru.practicum.category.model.Category;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.location.model.Location;
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

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Enumerated(EnumType.STRING)
    private EventState state;

    @ManyToMany
    @JoinTable(name = "compilation_events", joinColumns = @JoinColumn(name = "event_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "compilation_id", referencedColumnName = "id"))
    private List<Compilation> compilations;

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