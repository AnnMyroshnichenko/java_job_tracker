package org.example.jobtracker.controller;

import lombok.RequiredArgsConstructor;
import org.example.jobtracker.dto.NoteDto;
import org.example.jobtracker.entity.Note;
import org.example.jobtracker.entity.Vacancy;
import org.example.jobtracker.service.VacancyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService vacancyService;

    @GetMapping
    public List<Vacancy> getAllVacancies() {
        return vacancyService.getAllVacancies();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vacancy> getVacancyById(@PathVariable Long id) {
        return ResponseEntity.ok(vacancyService.getVacancyById(id));
    }

    @PostMapping
    public Vacancy createVacancy(@RequestBody Vacancy vacancy,
                                 @RequestParam(defaultValue = "Manual") String sourceName) {
        return vacancyService.addVacancyManual(vacancy, sourceName);
    }

    @PostMapping("/parse")
    public String runParser(@RequestParam String query,
                            @RequestParam(required = false) String level,
                            @RequestParam(required = false) String city,
                            @RequestParam(required = false) String english, // <--- НОВЕ
                            @RequestParam(required = false) List<String> sources,
                            RedirectAttributes redirectAttributes) {

        if (sources == null || sources.isEmpty()) {
            sources = List.of("DOU", "Djinni");
        }

        int count = vacancyService.fetchAndSave(query, level, city, english, sources);

        if (count == 0) {
            redirectAttributes.addFlashAttribute("messageType", "danger");
            redirectAttributes.addFlashAttribute("message", "На жаль, за вашим запитом нових вакансій не знайдено 😔");
        } else {
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Успіх! Знайдено та додано нових вакансій: " + count);
        }

        return "redirect:/";
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vacancy> updateVacancy(@PathVariable Long id,
                                                 @RequestBody Vacancy vacancyDetails) {
        Vacancy existing = vacancyService.getVacancyById(id);
        existing.setTitle(vacancyDetails.getTitle());
        existing.setCompany(vacancyDetails.getCompany());
        existing.setLink(vacancyDetails.getLink());
        existing.setStatus(vacancyDetails.getStatus());
        existing.setSalary(vacancyDetails.getSalary());
        existing.setDescription(vacancyDetails.getDescription());

        Vacancy updated = vacancyService.updateVacancy(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacancy(@PathVariable Long id) {
        vacancyService.deleteVacancy(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/notes")
    public Note addNote(@PathVariable Long id, @RequestBody NoteDto noteDTO) {
        return vacancyService.addNoteToVacancy(id, noteDTO.getContent());
    }

    @GetMapping("/{id}/notes")
    public List<Note> getNotes(@PathVariable Long id) {
        return vacancyService.getNotesForVacancy(id);
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId) {
        vacancyService.deleteNote(noteId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}