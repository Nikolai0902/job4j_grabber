package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import ru.job4j.grabber.utils.Post;

import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private final Properties cfg = new Properties();

    /**
     * Передает хранилищу PsqlStore ресурсный файл для работы.
     * @return возвращает хранилище.
     * @throws SQLException сключение вывода ресурсного файла.
     */
    public Store store() throws SQLException {
        return new PsqlStore(cfg);
    }

    /**
     * Создание класса управляющего всеми работами с переодичностью
      * @return возвращает данный класс.
     * @throws SchedulerException исключение при создании.
     */
    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    /**
     * Загрузка настроек, используется Properties.
     * @throws IOException исключение вывода ресурсного файла.
     */
    public void cfg() throws IOException {
        try (InputStream in = Grabber.class
                .getClassLoader()
                .getResourceAsStream("app.properties")) {
            cfg.load(in);
        }
    }

    /**
     * Запуск сбора вакансий и запись в таблицу БД.
     * Переодичность запуска указана в app.properties.
     * @param parse Извлечение данных с сайта.
     * @param store Хранилище.
     * @param scheduler Класса управляющего всеми работами с переодичностью.
     * @throws SchedulerException
     */
    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        data.put("pageLink", cfg.getProperty("PAGE_LINK"));
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {
        /**
         * Метод записывает вакансии в таблицу БД.
         * @param context объект с типом org.quartz.Job.
         * @throws JobExecutionException исключение.
         */
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            String link = (String) map.get("pageLink");
            List<Post> postList = parse.list(link);
            for (Post post: postList) {
                store.save(post);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.init(new HabrCareerParse(new HabrCareerDateTimeParser()), store, scheduler);
    }
}
