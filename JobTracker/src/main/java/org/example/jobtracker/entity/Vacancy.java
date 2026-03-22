package org.example.jobtracker.entity;

import jakarta.persistence.*;
import lombok.Data; // Импорт Lombok
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vacancies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String company;

    private String salary;

    private String city;
    private String level;
    private String technology;

    @Column(name = "english_level")
    private String englishLevel;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(unique = true, nullable = false)
    private String link;

    @Column(nullable = false)
    private Boolean isFavorite = false;

    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;

    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    @OneToMany(mappedBy = "vacancy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_id")
    private Source source;

    @PrePersist
    public void init() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = Status.NEW;
        }
    }
}