package ru.job4j.cinema.persistence;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.job4j.cinema.model.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class SessionDBStore {
    private static final Logger LOG = LoggerFactory.getLogger(SessionDBStore.class);

    private final BasicDataSource pool;

    public SessionDBStore(BasicDataSource pool) {
        this.pool = pool;
    }

    public Optional<Session> create(Session session) {
        LOG.info("Сохранение сеанса {}:{}", session.getId(), session.getName());
        String sql = ("INSERT INTO sessions(name) VALUES (?);");
        Optional<Session> result = Optional.empty();
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, session.getName());
            statement.execute();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    session.setId(resultSet.getInt(1));
                    result = Optional.of(session);
                }
            }
        } catch (SQLException e) {
            LOG.error("Не удалось выполнить операцию { }", e.getCause());
        }
        return result;
    }

    public Optional<Session> update(Session session) {
        LOG.info("Обновление сеанса {}:{}", session.getId(), session.getName());
        String sql = "UPDATE sessions SET name = ? WHERE id = ?;";
        Optional<Session> result = Optional.empty();
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(2, session.getId());
            statement.setString(1, session.getName());
            if (statement.executeUpdate() > 0) {
                result = Optional.of(session);
            }
        } catch (SQLException e) {
            LOG.error("Не удалось выполнить операцию { }", e.getCause());
        }
        return result;
    }

    public Optional<Session> findById(int id) {
        LOG.info("Поиск сеанса по ID:{}", id);
        String sql = "SELECT * FROM sessions WHERE id = ?;";
        Optional<Session> result = Optional.empty();
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result = Optional.of(getSession(resultSet));
                }
            }
        } catch (SQLException e) {
            LOG.error("Не удалось выполнить операцию { }", e.getCause());
        }
        return result;
    }

    public Optional<Session> delete(Session session) {
        LOG.info("Удаление сеанса {}:{}", session.getId(), session.getName());
        String sql = "DELETE FROM sessions WHERE id = ?;";
        Optional<Session> result = Optional.empty();
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, session.getId());
            if (statement.executeUpdate() > 0) {
                result = Optional.of(session);
            }
        } catch (SQLException e) {
            LOG.error("Не удалось выполнить операцию { }", e.getCause());
        }
        return result;
    }

    public List<Session> findAll() {
        LOG.info("Создание списка всех сеансов");
        List<Session> sessionList = new ArrayList<>();
        String sql = "SELECT * FROM sessions;";
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sessionList.add(getSession(resultSet));
                }
            }
        } catch (SQLException e) {
            LOG.error("Не удалось выполнить операцию { }", e.getCause());
        }
        return sessionList;
    }

    private Session getSession(ResultSet resultSet) throws SQLException {
        return new Session(resultSet.getInt("session_id"),
                resultSet.getString("session_name"));
    }

}