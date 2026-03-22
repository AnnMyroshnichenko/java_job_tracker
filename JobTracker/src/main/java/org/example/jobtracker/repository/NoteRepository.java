package org.example.jobtracker.repository;

import org.example.jobtracker.entity.Note;
import org.example.jobtracker.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByVacancy(Vacancy vacancy);
}
