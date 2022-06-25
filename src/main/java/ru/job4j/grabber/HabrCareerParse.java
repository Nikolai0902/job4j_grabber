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
import java.util.concurrent.atomic.AtomicInteger;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=",
            SOURCE_LINK);
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse hCP = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> result = hCP.list(PAGE_LINK);
        for (Post post: result) {
            System.out.println(post);
        }
    }

    @Override
    public List<Post> list(String link) {
        List<Post> result = new ArrayList<>();
        try {
            for (int i = 1; i < 2; i++) {
                Connection connection = Jsoup.connect(link + i);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                AtomicInteger id = new AtomicInteger();
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element linkElement = titleElement.child(0);
                    Element dateElement = row.select(".vacancy-card__date").first();
                    Element date = dateElement.child(0);
                    String vacancyName = titleElement.text();
                    String linkVac = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                    String vacancyDate = String.format("%s", date.attr("datetime"));
                    String description = retrieveDescription(linkVac);
                    result.add(new Post(id.getAndIncrement(), vacancyName, linkVac, description,
                            dateTimeParser.parse(vacancyDate)));
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String retrieveDescription(String link) {
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

