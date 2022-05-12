package ru.job4j.cinema.service;

import org.springframework.stereotype.Service;
import ru.job4j.cinema.model.User;
import ru.job4j.cinema.persistence.UserDBStore;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserDBStore store;

    public UserService(UserDBStore store) {
        this.store = store;
    }

    public Optional<User> create(User user) {
        return store.create(user);
    }

    public Optional<User> update(User user) {
        return store.update(user);
    }

    public Optional<User> findById(int id) {
        return store.findById(id);
    }

    public List<User> findAll() {
        return store.findAll();
    }

    public Optional<User> findUserByEmailAndPhone(User user) {
        return store.findUserByEmailAndPhone(user.getEmail(), user.getPhone());
    }
}