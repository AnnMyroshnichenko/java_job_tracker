package org.example.jobtracker.controller;

import lombok.RequiredArgsConstructor;
import org.example.jobtracker.service.VacancyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.example.jobtracker.entity.Status;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final VacancyService vacancyService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("vacancies", vacancyService.getAllVacancies());

        // Додаємо перевірку: якщо сервіс повернув null, передаємо порожній список
        var favorites = vacancyService.getFavoriteVacancies();
        model.addAttribute("favorites", favorites != null ? favorites : java.util.Collections.emptyList());

        return "index";
    }

    @GetMapping("/view/{id}")
    public String viewVacancy(@PathVariable Long id, Model model) {
        model.addAttribute("vacancy", vacancyService.getVacancyById(id));
        return "details";
    }

    @PostMapping("/parse")
    public String runParser(@RequestParam String query,
                            @RequestParam(required = false) String level,
                            @RequestParam(required = false) String city,
                            @RequestParam(required = false) String english,
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

    @PostMapping("/status/update")
    public String updateStatus(@RequestParam Long id,
                               @RequestParam Status status,
                               @org.springframework.web.bind.annotation.RequestHeader(value = "Referer", required = false) String referer) {
        vacancyService.updateStatus(id, status);
        return "redirect:" + (referer != null ? referer : "/");
    }

    @GetMapping("/favorite/{id}")
    public String toggleFavorite(@PathVariable Long id,
                                 @org.springframework.web.bind.annotation.RequestHeader(value = "Referer", required = false) String referer) {
        vacancyService.toggleFavorite(id);
        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("/note/add")
    public String addNote(@RequestParam Long vacancyId, @RequestParam String content) {
        vacancyService.addNoteToVacancy(vacancyId, content);
        return "redirect:/view/" + vacancyId;
    }
}