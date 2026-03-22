package org.example.jobtracker.service;

import org.example.jobtracker.entity.Vacancy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DouParserService {

    private static final String BASE_URL = "https://jobs.dou.ua/vacancies/?";

    public List<Vacancy> parse(String searchQuery, String level, String city) {
        List<Vacancy> vacancies = new ArrayList<>();

        StringBuilder urlBuilder = new StringBuilder(BASE_URL);

        if (searchQuery != null && !searchQuery.isEmpty()) {
            urlBuilder.append("search=")
                    .append(URLEncoder.encode(searchQuery, StandardCharsets.UTF_8));
        }

        if (level != null && !level.isEmpty()) {
            String expCode = mapLevelToCode(level);
            if (!expCode.isEmpty()) {
                urlBuilder.append("&exp=").append(expCode);
            }
        }

        if (city != null && !city.isEmpty()) {
            urlBuilder.append("&city=")
                    .append(URLEncoder.encode(city, StandardCharsets.UTF_8));
        }

        String finalUrl = urlBuilder.toString();
        System.out.println("Парсим URL: " + finalUrl);

        try {
            Document doc = Jsoup.connect(finalUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .referrer("https://google.com")
                    .get();

            Elements vacancyElements = doc.select("li.l-vacancy");

            for (Element el : vacancyElements) {
                Vacancy vacancy = new Vacancy();

                Element titleElement = el.selectFirst(".title a.vt");
                if (titleElement != null) {
                    vacancy.setTitle(titleElement.text());
                    vacancy.setLink(titleElement.attr("href"));
                }

                Element companyElement = el.selectFirst("a.company");
                if (companyElement != null) vacancy.setCompany(companyElement.text());

                Element salaryElement = el.selectFirst(".salary");
                if (salaryElement != null) vacancy.setSalary(salaryElement.text());

                Element descElement = el.selectFirst(".sh-info");
                if (descElement != null) vacancy.setDescription(descElement.text());

                if (vacancy.getLink() != null) {
                    vacancies.add(vacancy);
                }

                String descLower = vacancy.getDescription().toLowerCase();
                if (descLower.contains("upper")) {
                    vacancy.setEnglishLevel("Upper-Intermediate");
                } else if (descLower.contains("intermediate")) {
                    vacancy.setEnglishLevel("Intermediate");
                } else if (descLower.contains("pre-intermediate")) {
                    vacancy.setEnglishLevel("Pre-Intermediate");
                } else if (descLower.contains("fluent") || descLower.contains("advanced")) {
                    vacancy.setEnglishLevel("Advanced / Fluent");
                } else {
                    vacancy.setEnglishLevel("Не вказано");
                }
            }

        } catch (IOException e) {
            System.err.println("Ошибка парсинга: " + e.getMessage());
        }

        return vacancies;
    }

    private String mapLevelToCode(String level) {
        if (level == null) return "";
        switch (level.toLowerCase()) {
            case "0": return "0-1";
            case "1": return "0-1";
            case "2": return "1-3";
            case "3": return "1-3";
            case "5": return "5plus";
            default: return "";
        }
    }
}