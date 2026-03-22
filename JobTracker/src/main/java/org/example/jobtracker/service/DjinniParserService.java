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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DjinniParserService {

    private static final String BASE_URL = "https://djinni.co/jobs/?";

    public List<Vacancy> parse(String searchQuery, String level, String city, String english) {
        List<Vacancy> vacancies = new ArrayList<>();
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);

        if (searchQuery != null && !searchQuery.isEmpty()) {
            urlBuilder.append("primary_keyword=").append(URLEncoder.encode(searchQuery, StandardCharsets.UTF_8));
        }
        if (level != null && !level.isEmpty()) {
            String expCode = mapLevelToDjinni(level);
            if (!expCode.isEmpty()) urlBuilder.append("&exp_level=").append(expCode);
        }
        if (city != null && !city.isEmpty()) {
            urlBuilder.append("&location=").append(URLEncoder.encode(city.toLowerCase(), StandardCharsets.UTF_8));
        }
        if (english != null && !english.isEmpty() && !english.equals("no_english")) {
            urlBuilder.append("&english_level=").append(english);
        }

        String finalUrl = urlBuilder.toString();
        System.out.println(">>> DJINNI URL: " + finalUrl);

        try {
            Document doc = Jsoup.connect(finalUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
                    .header("Accept-Language", "uk-UA,uk;q=0.9,en-US;q=0.8,en;q=0.7")
                    .referrer("https://djinni.co/")
                    .ignoreHttpErrors(true)
                    .get();

            Elements vacancyElements = doc.select("li[id^='job-item-']");
            System.out.println(">>> НАЙДЕНО ЭЛЕМЕНТОВ: " + vacancyElements.size());

            for (Element el : vacancyElements) {
                Vacancy vacancy = new Vacancy();

                Element titleEl = el.selectFirst(".job-item__title-link");
                if (titleEl != null) {
                    vacancy.setTitle(titleEl.text());
                    String href = titleEl.attr("href");
                    if (!href.startsWith("http")) href = "https://djinni.co" + href;
                    vacancy.setLink(href);
                } else {
                    continue;
                }

                Element descEl = el.selectFirst(".js-truncated-text");
                if (descEl != null) {
                    vacancy.setDescription(descEl.text());
                } else {
                    String rawText = el.text();
                    vacancy.setDescription(rawText.length() > 200 ? rawText.substring(0, 200) + "..." : rawText);
                }

                Element companyEl = el.selectFirst("a[href*='/jobs/company-']");
                if (companyEl != null) {
                    vacancy.setCompany(companyEl.text().trim());
                } else {
                    vacancy.setCompany("Djinni User");
                }

                Element salaryEl = el.selectFirst(".text-success");
                if (salaryEl != null) {
                    vacancy.setSalary(salaryEl.text().replace("до", "").trim());
                }
                if (english != null && !english.isEmpty()) {
                    vacancy.setEnglishLevel(english);
                }

                if (level != null && !level.isEmpty()) {
                    vacancy.setLevel(level);
                }

                vacancies.add(vacancy);
            }

        } catch (IOException e) {
            System.err.println(">>> ERROR: " + e.getMessage());
        }

        return vacancies;
    }

    private String mapLevelToDjinni(String level) {
        if (level == null) return "";
        switch (level.toLowerCase()) {
            case "0": return "no_exp";
            case "1": return "1y";
            case "2": return "2y";
            case "3": return "3y";
            case "5": return "5y";
            default: return "";
        }
    }
}