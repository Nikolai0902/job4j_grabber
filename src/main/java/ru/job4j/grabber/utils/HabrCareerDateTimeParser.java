package ru.job4j.grabber.utils;


import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String st) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(st);
        return zonedDateTime.toLocalDateTime();
    }
}
