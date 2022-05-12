package ru.job4j.cinema.persistence;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.job4j.cinema.model.Session;
import ru.job4j.cinema.model.Ticket;
import ru.job4j.cinema.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TicketDBStore {
    private static final Logger LOG = LoggerFactory.getLogger(SessionDBStore.class);

    private final BasicDataSource pool;

    public TicketDBStore(BasicDataSource pool) {
        this.pool = pool;
    }

    public Optional<Ticket> create(Ticket ticket) {
        LOG.info("Сохранение билета {}:{}", ticket.getId(), ticket.getSession().getName());
        String sql = ("INSERT INTO ticket (session_id, rowr, cell, user_id) VALUES (?, ?, ?, ?);");
        Optional<Ticket> result = Optional.empty();
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, ticket.getSession().getId());
            statement.setInt(2, ticket.getRow());
            statement.setInt(3, ticket.getCell());
            statement.setInt(4, ticket.getUser().getId());
            statement.execute();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    ticket.setId(resultSet.getInt(1));
                    result = Optional.of(ticket);
                }
            }
        } catch (SQLException e) {
            LOG.error("Не удалось выполнить операцию {}", e.getMessage());
        }
        return result;
    }

    public Optional<Ticket> update(Ticket ticket) {
        LOG.info("Обновление билета {}:{}", ticket.getId(), ticket.getSession().getName());
        String sql = "UPDATE ticket SET session_id = ?, rowr = ?, cell = ?, user_id = ? WHERE ticket_id = ?;";
        Optional<Ticket> result = Optional.empty();
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(5, ticket.getId());
            statement.setInt(1, ticket.getSession().getId());
            statement.setInt(2, ticket.getRow());
            statement.setInt(3, ticket.getCell());
            statement.setInt(4, ticket.getUser().getId());
            if (statement.executeUpdate() > 0) {
                result = Optional.of(ticket);
            }
        } catch (SQLException e) {
            LOG.error("Не удалось выполнить операцию {}", e.getMessage());
        }
        return result;
    }

    public Optional<Ticket> findById(int id) {
        LOG.info("Поиск билета по ID:{}", id);
        String sql = "SELECT * FROM ticket AS t "
                + "INNER JOIN users AS u "
                + "USING (user_id) "
                + "INNER JOIN sessions AS s "
                + "USING (session_id) "
                + "WHERE t.ticket_id = ?;";
        Optional<Ticket> result = Optional.empty();
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result = Optional.of(getTicket(resultSet));
                }
            }
        } catch (SQLException e) {
            LOG.error("Не удалось выполнить операцию {}", e.getMessage());
        }
        return result;
    }

    public Optional<Ticket> delete(Ticket ticket) {
        LOG.info("Удаление билета {}:{}", ticket.getId(), ticket.getSession().getName());
        String sql = "DELETE FROM ticket WHERE ticket_id = ?;";
        Optional<Ticket> result = Optional.empty();
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ticket.getId());
            if (statement.executeUpdate() > 0) {
                result = Optional.of(ticket);
            }
        } catch (SQLException e) {
            LOG.error("Не удалось выполнить операцию {}", e.getMessage());
        }
        return result;
    }

    public List<Ticket> findAll() {
        LOG.info("Создание списка всех билетов");
        List<Ticket> ticketList = new ArrayList<>();
        String sql = "SELECT * FROM ticket AS t "
                + "INNER JOIN users AS u "
                + "USING (user_id) "
                + "INNER JOIN sessions AS s "
                + "USING (session_id);";
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ticketList.add(getTicket(resultSet));
                }
            }
        } catch (SQLException e) {
            LOG.error("Не удалось выполнить операцию {}", e.getMessage());
        }
        return ticketList;
    }

    public Map<Integer, Map<Integer, Ticket>> findTicketSession(int idSession) {
        LOG.info("Начала поиска билетов на сеанс {}", idSession);
        Map<Integer, Map<Integer, Ticket>> result = new ConcurrentHashMap<>();
        String sql = "SELECT * FROM ticket AS t "
                + "INNER JOIN users AS u "
                + "USING (user_id) "
                + "INNER JOIN sessions AS s "
                + "USING (session_id) "
                + "WHERE t.session_id = ?"
                + "ORDER BY t.rowr ASC, t.cell ASC;";
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idSession);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Ticket ticket = getTicket(resultSet);
                    result.putIfAbsent(ticket.getRow(), new HashMap<>());
                    result.get(ticket.getRow()).put(ticket.getCell(), ticket);
                }
            }
        } catch (SQLException e) {
            LOG.error("Не удалось выполнить операцию {}", e.getMessage());
        }
        return result;
    }

    public Optional<Ticket> findBySessionRowCell(int sessionId, int row, int cell) {
        LOG.info("Поиск билета по сеансу:{}, ряду:{}, месту:{}", sessionId, row, cell);
        String sql = "SELECT * FROM ticket AS t "
                + "INNER JOIN users AS u "
                + "USING (user_id) "
                + "INNER JOIN sessions AS s "
                + "USING (id) "
                + "WHERE t.id = ? AND t.rowr = ? AND t.cell = ?;";
        Optional<Ticket> result = Optional.empty();
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sessionId);
            statement.setInt(2, row);
            statement.setInt(3, cell);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result = Optional.of(getTicket(resultSet));
                }
            }
        } catch (SQLException e) {
            LOG.error("Не удалось выполнить операцию {}", e.getMessage());
        }
        return result;
    }

    public List<Ticket> findTicketByUser(User user) {
        LOG.info("Начала поиска билетов пользователя {}", user.getEmail());
        List<Ticket> ticketList = new ArrayList<>();
        String sql = "SELECT * FROM ticket AS t "
                + "INNER JOIN users AS u "
                + "ON t.user_id = u.user_id AND t.user_id = ? "
                + "INNER JOIN sessions AS s "
                + "USING (session_id);";
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ticketList.add(getTicket(resultSet));
                }
            }
        } catch (SQLException e) {
            LOG.error("Не удалось выполнить операцию {}", e.getMessage());
        }
        return ticketList;
    }

    private Ticket getTicket(ResultSet resultSet) throws SQLException {
        return new Ticket(resultSet.getInt("ticket_id"),
                new Session(resultSet.getInt("session_id"),
                        resultSet.getString("session_name")),
                resultSet.getInt("rowr"),
                resultSet.getInt("cell"),
                new User(resultSet.getInt("user_id"),
                        resultSet.getString("user_name"),
                        resultSet.getString("email"),
                        resultSet.getString("phone")));
    }
}