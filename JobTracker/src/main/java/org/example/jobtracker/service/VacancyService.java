package org.example.jobtracker.service;

import lombok.RequiredArgsConstructor;
import org.example.jobtracker.entity.Note;
import org.example.jobtracker.entity.Source;
import org.example.jobtracker.entity.Status;
import org.example.jobtracker.entity.Vacancy;
import org.example.jobtracker.repository.NoteRepository;
import org.example.jobtracker.repository.SourceRepository;
import org.example.jobtracker.repository.VacancyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    private final SourceRepository sourceRepository;
    private final NoteRepository noteRepository;

    private final DouParserService douParserService;
    private final DjinniParserService djinniParserService;

    @Transactional
    public int fetchAndSave(String searchQuery, String level, String city, String english, List<String> sourcesToParse) {
        int totalNew = 0;

        if (sourcesToParse == null || sourcesToParse.contains("DOU")) {
            List<Vacancy> douVacancies = douParserService.parse(searchQuery, level, city);
            totalNew += processVacancies(douVacancies, "DOU", searchQuery, level, city);
        }

        if (sourcesToParse == null || sourcesToParse.contains("Djinni")) {
            List<Vacancy> djinniVacancies = djinniParserService.parse(searchQuery, level, city, english);
            totalNew += processVacancies(djinniVacancies, "Djinni", searchQuery, level, city);
        }

        return totalNew;
    }

    private int processVacancies(List<Vacancy> vacancies, String sourceName, String query, String level, String city) {
        int count = 0;
        for (Vacancy v : vacancies) {
            v.setTechnology(query);
            if (level != null) v.setLevel(level);
            if (v.getCity() == null && city != null) v.setCity(city);

            if (saveVacancyFromParser(v, sourceName)) {
                count++;
            }
        }
        return count;
    }

    @Transactional
    public boolean saveVacancyFromParser(Vacancy vacancy, String sourceName) {
        Optional<Vacancy> existingOpt = vacancyRepository.findByLink(vacancy.getLink());
        if (existingOpt.isPresent()) {
            return false;
        }
        Source source = getOrCreateSource(sourceName);
        vacancy.setSource(source);
        vacancy.setStatus(Status.NEW);
        vacancyRepository.save(vacancy);
        return true;
    }

    public List<Vacancy> getFavoriteVacancies() {
        List<Vacancy> allVacancies = vacancyRepository.findAll();

        if (allVacancies == null) {
            return java.util.Collections.emptyList();
        }

        return allVacancies.stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsFavorite()))
                .toList();
    }

    private Source getOrCreateSource(String sourceName) {
        return sourceRepository.findByName(sourceName)
                .orElseGet(() -> {
                    Source newSource = new Source();
                    newSource.setName(sourceName);
                    return sourceRepository.save(newSource);
                });
    }

    @Transactional
    public void toggleFavorite(Long id) {
        Vacancy vacancy = getVacancyById(id);
        if (vacancy.getIsFavorite() == null) {
            vacancy.setIsFavorite(true);
        } else {
            vacancy.setIsFavorite(!vacancy.getIsFavorite());
        }
        vacancyRepository.save(vacancy);
    }


    public List<Vacancy> getAllVacancies() {
        return vacancyRepository.findAll();
    }

    public Vacancy getVacancyById(Long id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacancy not found with id: " + id));
    }

    @Transactional
    public Vacancy addVacancyManual(Vacancy vacancy, String sourceName) {
        if (vacancy.getLink() != null && vacancyRepository.findByLink(vacancy.getLink()).isPresent()) {
            throw new RuntimeException("Vacancy with this link already exists");
        }

        Source source = getOrCreateSource(sourceName);
        vacancy.setSource(source);
        if (vacancy.getStatus() == null) {
            vacancy.setStatus(Status.NEW);
        }
        return vacancyRepository.save(vacancy);
    }

    @Transactional
    public void updateStatus(Long id, Status newStatus) {
        Vacancy vacancy = getVacancyById(id);
        vacancy.setStatus(newStatus);
        vacancyRepository.save(vacancy);
    }

    public void deleteVacancy(Long id) {
        vacancyRepository.deleteById(id);
    }

    @Transactional
    public Note addNoteToVacancy(Long vacancyId, String content) {
        Vacancy vacancy = getVacancyById(vacancyId);

        Note note = new Note();
        note.setContent(content);
        note.setVacancy(vacancy);

        return noteRepository.save(note);
    }

    public List<Note> getNotesForVacancy(Long vacancyId) {
        Vacancy vacancy = getVacancyById(vacancyId);
        return noteRepository.findByVacancy(vacancy);
    }

    @Transactional
    public Note editNote(Long noteId, String newContent) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        note.setContent(newContent);
        return noteRepository.save(note);
    }

    public void deleteNote(Long noteId) {
        noteRepository.deleteById(noteId);
    }

    public Vacancy updateVacancy(Vacancy vacancy) {
        return vacancyRepository.save(vacancy);
    }
}