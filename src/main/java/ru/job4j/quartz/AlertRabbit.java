package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    public static void main(String[] args) throws Exception {
        try {
            Properties properties = fileRead();
            try (Connection connection = getConnection(properties)) {
                Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                JobDataMap data = new JobDataMap();
                data.put("connection", connection);
                JobDetail job = newJob(Rabbit.class)
                        .usingJobData(data)
                        .build();
                SimpleScheduleBuilder times = simpleSchedule()
                        .withIntervalInSeconds(Integer.parseInt(properties
                                .getProperty("rabbit.interval")))
                        .repeatForever();
                Trigger trigger = newTrigger()
                        .startNow()
                        .withSchedule(times)
                        .build();
                scheduler.scheduleJob(job, trigger);
                Thread.sleep(10000);
                scheduler.shutdown();
            }
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context)  {
            System.out.println("Rabbit runs here ...");
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("connection");
                try (PreparedStatement statement =
                             cn.prepareStatement("insert into rabbit(created_date) values (?)")) {
                    statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Properties fileRead() throws Exception {
        Properties properties = new Properties();
        try (InputStream in = AlertRabbit.class
                .getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            properties.load(in);
        }
        return properties;
    }

    private static Connection getConnection(Properties file) throws Exception {
        Class.forName(file.getProperty("jdbc.driver"));
        String url = file.getProperty("jdbc.url");
        String login = file.getProperty("jdbc.username");
        String password = file.getProperty("jdbc.password");
        return DriverManager.getConnection(url, login, password);
    }
}
