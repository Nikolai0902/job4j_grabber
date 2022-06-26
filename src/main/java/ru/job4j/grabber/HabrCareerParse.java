package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import ru.job4j.grabber.utils.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=",
            SOURCE_LINK);
    private static final Integer STR = 5;
    private final DateTimeParser dateTimeParser;

    /**
     * Парсер даты в поле, принисается в конструкторе.
     * Используется для парсинга конретного формата даты,
     * можно изменить и использовать другой при создании HabrCareerParse.
     * @param dateTimeParser парсер даты
     */
    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse hCP = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> result = hCP.list(PAGE_LINK);
        for (Post post : result) {
            System.out.println(post);
        }
    }

    /**
     * Возвращает массив оъектов Post(вакансий)
     * с первых пяти страниц сайта.
     * @param link ссылка страницы всех вакансий
     * @return массив оъектов Post
     */
    @Override
    public List<Post> list(String link) {
        List<Post> result = new ArrayList<>();
        try {
            for (int i = 1; i <= STR; i++) {
                Connection connection = Jsoup.connect(link + i);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Post post = returnPost(row);
                    result.add(post);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Собирает элемент Post при парсинге.
     * @param element вакансии
     * @return элемент Post
     */
    private Post returnPost(Element element) {
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        Element dateElement = element.select(".vacancy-card__date").first();
        Element date = dateElement.child(0);
        String vacancyName = titleElement.text();
        String linkVac = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String vacancyDate = String.format("%s", date.attr("datetime"));
        String description = retrieveDescription(linkVac);
        return new Post(vacancyName, linkVac, description,
                dateTimeParser.parse(vacancyDate));
    }

    /**
     * Возвращает описание ваканисии(description) при переходе
     * на страницу описания конретной вакансии.
     * @param link ссылка страницы одной вакансии
     * @return описание вакансии
     */
    private String retrieveDescription(String link) {
        String descriptionText = null;
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            Element rows = document.select(".job_show_description").first();
            descriptionText = rows.text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return descriptionText;
    }
}

