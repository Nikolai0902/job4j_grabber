package ru.job4j.grabber;

import ru.job4j.grabber.utils.Post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;

    public PsqlStore(Properties cfg) throws SQLException {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        cnn = DriverManager.getConnection(
                cfg.getProperty("jdbc.url"),
                cfg.getProperty("jdbc.username"),
                cfg.getProperty("jdbc.password"));
    }

    /**
     * Код вставки в таблицу post.
     * @param post вакансия.
     */
    @Override
    public void save(Post post) {
        try (PreparedStatement statement = cnn
                .prepareStatement("insert into post(name, text, link, created) "
                                             + "values (?, ?, ?, ?) on conflict(link) do nothing",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Вывести все вакансии из БД.
     * @return массив вакансий.
     */
    @Override
    public List<Post> getAll() {
        List<Post> resultPost = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement("select * from post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resultPost.add(resultPost(resultSet));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultPost;
    }

    /**
     * Вывести вакансию по id из БД.
     * @param id индекс вакансии.
     * @return вакансия.
     */
    @Override
    public Post findById(int id) {
        Post result = null;
        try (PreparedStatement statement = cnn.prepareStatement(
                "select * from post where id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result = resultPost(resultSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    /**
     * Отдельный метод для получения элемента вакансии Post
     * для методов findById и getAll.
     * @param resultSet Объект для обхода элементов из таблицы БД.
     * @return вакансия.
     * @throws SQLException неверный запрос.
     */
    private static Post resultPost(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("text"),
                resultSet.getString("link"),
                resultSet.getTimestamp("created").toLocalDateTime()
        );
    }
}
