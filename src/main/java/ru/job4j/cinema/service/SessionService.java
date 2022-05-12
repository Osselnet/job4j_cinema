package ru.job4j.cinema.service;


import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Service;
import ru.job4j.cinema.model.Session;
import ru.job4j.cinema.persistence.SessionDBStore;

import java.util.List;
import java.util.Optional;

@ThreadSafe
@Service
public class SessionService {
    private final SessionDBStore store;

    public SessionService(SessionDBStore store) {
        this.store = store;
    }

    public Optional<Session> create(Session session) {
        return store.create(session);
    }

    public Optional<Session> update(Session session) {
        return store.update(session);
    }

    public Optional<Session> findById(int id) {
        return store.findById(id);
    }

    public List<Session> findAll() {
        return store.findAll();
    }
}
