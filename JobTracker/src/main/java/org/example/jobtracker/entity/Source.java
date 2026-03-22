package org.example.jobtracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "sources")

@Data
@NoArgsConstructor
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String baseUrl;

    @OneToMany(mappedBy = "source")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Vacancy> vacancies;
}
