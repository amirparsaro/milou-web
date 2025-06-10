package com.milou.spring_boot.service;

import com.milou.spring_boot.exception.*;
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

    public static User getUser(Session session, Integer id) throws UserNotFoundException {
        List<User> allUsers = session.createNativeQuery("select * from users where id = :given_id", User.class)
                .setParameter("given_id", id)
                .getResultList();

        if (allUsers.isEmpty()) {
            throw new UserNotFoundException(id);
        }

        return allUsers.getFirst();
    }

    public static User getUser(Integer id) throws UserNotFoundException {
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

    public static User getUserByEmailPassword(String email, String password) throws UserNotFoundException {
        List<User> allUsers = sessionFactory.fromTransaction(session -> {
            return session.createNativeQuery("select * from users " +
                            "where email = :given_email and " +
                            "password = :given_password", User.class)
                    .setParameter("given_email", email)
                    .setParameter("given_password", password)
                    .getResultList();
        });

        if (allUsers.isEmpty()) {
            throw new UserNotFoundException();
        }

        return allUsers.getFirst();
    }

    public static User getUserByEmail(String name) throws UserNotFoundException {
        List<User> allUsers = sessionFactory.fromTransaction(session -> {
            return session.createNativeQuery("select * from users " +
                            "where email = :given_email", User.class)
                    .setParameter("given_email", name)
                    .getResultList();
        });

        if (allUsers.isEmpty()) {
            throw new UserNotFoundException();
        }

        return allUsers.getFirst();
    }

    public static void deleteUser(Integer id) throws UserNotFoundException {
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

    public static void addUser(User user) {
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

    public static void updateUser(User user) throws UserNotFoundException {
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

    public static User logIn(String email, String password) throws InvalidCredentialsException {
        if (!(email.endsWith("@milou.com")))
            email += "@milou.com";

        User user = null;
        try {
            user = getUserByEmailPassword(email, password);
            if (user != null) {
                return user;
            }
        } catch (UserNotFoundException e) {}

        throw new InvalidCredentialsException("User not found. Email or Password might be wrong.");
    }

    public static User signUp(String name, String email, String password) throws InvalidRegistrationException {
        User user = null;
        if (!(email.endsWith("@milou.com")))
            email += "@milou.com";

        if (password.length() < 8)
            throw new InvalidRegistrationException("Password length is less than 8 characters.");

        try {
            if (getUserByEmail(email) != null)
                throw new InvalidRegistrationException("Email already exists.");
        } catch (UserNotFoundException e) {}

        user = new User(name, email, password);
        addUser(user);

        return user;
    }
}
