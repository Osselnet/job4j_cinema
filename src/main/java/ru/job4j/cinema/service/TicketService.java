package ru.job4j.cinema.service;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Service;
import ru.job4j.cinema.model.Session;
import ru.job4j.cinema.model.Ticket;
import ru.job4j.cinema.model.User;
import ru.job4j.cinema.persistence.TicketDBStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
@Service
public class TicketService {
    /**
     * Количество рядов в зале
     */
    private static final int ROW = 5;
    /**
     * Количество мест в зале
     */
    private static final int CELL = 5;
    /**
     * Хранилище купленных билетов.
     */
    private final TicketDBStore store;

    public TicketService(TicketDBStore store) {
        this.store = store;
    }

    public Optional<Ticket> create(Ticket ticket) {
        return store.create(ticket);
    }

    public Optional<Ticket> findById(int idTicket) {
        return store.findById(idTicket);
    }

    public Optional<Ticket> delete(Ticket ticket) {
        return store.delete(ticket);
    }

    public List<Ticket> findAllTicket() {
        return store.findAll();
    }

    public Map<Integer, Map<Integer, Ticket>> findTicketSession(int sessionId) {
        return store.findTicketSession(sessionId);
    }

    public Map<Integer, Map<Integer, Ticket>> findFreeTicketSession(Session session) {
        Map<Integer, Map<Integer, Ticket>> result = initTicket(session);
        Map<Integer, Map<Integer, Ticket>> allTicket = findTicketSession(session.getId());
        for (Integer key : allTicket.keySet()) {
            for (Integer k : allTicket.get(key).keySet()) {
                result.get(key).remove(k);
            }
            if (result.get(key).size() == 0) {
                result.remove(key);
            }
        }
        return result;
    }

    private Map<Integer, Map<Integer, Ticket>> initTicket(Session session) {
        Map<Integer, Map<Integer, Ticket>> result = new HashMap<>();
        for (int i = 1; i <= ROW; i++) {
            result.putIfAbsent(i, new ConcurrentHashMap<>());
            for (int j = 1; j <= CELL; j++) {
                result.get(i).putIfAbsent(j, new Ticket(0, session, i, j, null));
            }
        }
        return result;
    }

    public Optional<Ticket> findBySessionRowCell(int sessionId, int row, int cell) {
        return store.findBySessionRowCell(sessionId, row, cell);
    }

    public List<Ticket> findTicketByUser(User user) {
        return store.findTicketByUser(user);
    }
}