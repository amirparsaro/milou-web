package com.milou.spring_boot.service;

import com.milou.spring_boot.exception.UserAlreadyExistsException;
import com.milou.spring_boot.exception.UserNotFoundException;
import com.milou.spring_boot.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class UserService {
    private static SessionFactory sessionFactory;

    private static void setUpSessionFactory() {
        sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();
    }

    private static void closeSessionFactory() {
        sessionFactory.close();
    }

    public User getUser(Session session, Integer id) throws UserNotFoundException {
        List<User> allUsers = session.createNativeQuery("SELECT * FROM users WHERE id = :given_id", User.class)
                .setParameter("given_id", id)
                .getResultList();

        if (allUsers.isEmpty()) {
            throw new UserNotFoundException(id);
        }

        return allUsers.getFirst();
    }

    public User getUser(Integer id) throws UserNotFoundException {
        setUpSessionFactory();

        User user = sessionFactory.fromTransaction(session -> {
            try {
                return getUser(session, id);
            } catch (UserNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        closeSessionFactory();

        return user;
    }

    public void deleteUser(Integer id) throws UserNotFoundException {
        setUpSessionFactory();
        sessionFactory.inTransaction(session -> {
            try {
                User user = getUser(session, id);
            } catch (UserNotFoundException e) {
                throw new RuntimeException(e);
            }

            session.createNativeMutationQuery("delete from users " +
                            "where id = :given_id")
                    .setParameter("given_id", id)
                    .executeUpdate();
        });
        closeSessionFactory();
    }

    public void addUser(User user) {
        setUpSessionFactory();
        AtomicBoolean userFound = new AtomicBoolean(true);
        sessionFactory.inTransaction(session -> {
            try {
                User userFromDb = getUser(session, user.getId());
            } catch (UserNotFoundException e) {
                userFound.set(false);
            }

            if (userFound.get()) {
                throw new UserAlreadyExistsException(user.getId());
            }

            session.createNativeMutationQuery("insert into users (name, password, email) " +
                            "values (:given_name, :given_password, :given_email)")
                    .setParameter("given_name", user.getName())
                    .setParameter("given_password", user.getPassword())
                    .setParameter("given_email", user.getEmail())
                    .executeUpdate();
        });
        closeSessionFactory();
    }

    public void updateUser(User user) throws UserNotFoundException {
        setUpSessionFactory();

        sessionFactory.inTransaction(session -> {
            try {
                User existingUser = getUser(session, user.getId());
            } catch (UserNotFoundException e) {
                throw new RuntimeException(e);
            }

            session.createNativeMutationQuery(
                            "update users set name = :given_name, password = :given_password, email = :given_email " +
                                    "where id = :given_id")
                    .setParameter("given_name", user.getName())
                    .setParameter("given_password", user.getPassword())
                    .setParameter("given_email", user.getEmail())
                    .setParameter("given_id", user.getId())
                    .executeUpdate();
        });
    }
}
