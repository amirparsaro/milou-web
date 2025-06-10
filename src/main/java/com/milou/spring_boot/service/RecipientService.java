package com.milou.spring_boot.service;

import com.milou.spring_boot.exception.RecipientAlreadyExistsException;
import com.milou.spring_boot.exception.RecipientNotFoundException;
import com.milou.spring_boot.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RecipientService {
    private static SessionFactory sessionFactory;

    private static void setUpSessionFactory() {
        sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();
    }

    private static void closeSessionFactory() {
        sessionFactory.close();
    }

    public static Recipient getRecipient(Session session, Integer id) throws RecipientNotFoundException {
        List<Recipient> allRecipients = session.createNativeQuery("select * from recipients where id = :given_id", Recipient.class)
                .setParameter("given_id", id)
                .getResultList();

        if (allRecipients.isEmpty()) {
            throw new RecipientNotFoundException(id);
        }

        return allRecipients.getFirst();
    }

    public static Recipient getRecipient(Integer id) throws RecipientNotFoundException {
        setUpSessionFactory();

        Recipient recipient = sessionFactory.fromTransaction(session -> {
            try {
                return getRecipient(session, id);
            } catch (RecipientNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        closeSessionFactory();

        return recipient;
    }

    public static Recipient getRecipient(String code) throws RecipientNotFoundException {
        setUpSessionFactory();

        Recipient recipient = sessionFactory.fromTransaction(session -> {
            try {
                int id = Integer.parseInt(code.toLowerCase(), 36);
                return getRecipient(session, id);
            } catch (RecipientNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        closeSessionFactory();

        return recipient;
    }

    public static void deleteRecipient(Integer id) throws RecipientNotFoundException {
        setUpSessionFactory();
        sessionFactory.inTransaction(session -> {
            try {
                Recipient recipient = getRecipient(session, id);
            } catch (RecipientNotFoundException e) {
                throw new RuntimeException(e);
            }

            session.createNativeMutationQuery("delete from recipients " +
                            "where id = :given_id")
                    .setParameter("given_id", id)
                    .executeUpdate();
        });
        closeSessionFactory();
    }

    public static void addRecipient(Recipient recipient) throws RecipientAlreadyExistsException {
        setUpSessionFactory();
        AtomicBoolean recipientFound = new AtomicBoolean(true);
        sessionFactory.inTransaction(session -> {
                    try {
                        Recipient recipientFromDb = getRecipient(session, recipient.getId());
                    } catch (RecipientNotFoundException e) {
                        recipientFound.set(false);
                    }

                    if (recipientFound.get()) {
                        throw new RecipientAlreadyExistsException(recipient.getId());
                    }

                    session.createNativeMutationQuery("insert into recipients (message_id, recipient_id) " +
                                    "values (:message_id, :recipient_id)")
                            .setParameter("message_id", recipient.getMessage().getId())
                            .setParameter("recipient_id", recipient.getRecipient().getId())
                            .executeUpdate();
                });
            closeSessionFactory();
    }

    public static void updateRecipient(Recipient recipient) throws RecipientNotFoundException {
        setUpSessionFactory();

        sessionFactory.inTransaction(session -> {
            try {
                Recipient existingRecipient = getRecipient(session, recipient.getId());
            } catch (RecipientNotFoundException e) {
                throw new RuntimeException(e);
            }

            session.createNativeMutationQuery("update recipients " +
                            "set message_id = :message_id, recipient_id = :recipient_id " +
                            "where id = :id")
                    .setParameter("message_id", recipient.getMessage().getId())
                    .setParameter("recipient_id", recipient.getRecipient().getId())
                    .setParameter("id", recipient.getId())
                    .executeUpdate();
        });
    }

    public static Recipient createRecipientFromUser(User user, Message message) {
        Recipient recipient = new Recipient(message, user);
        addRecipient(recipient);
        return recipient;
    }

    public static List<Recipient> createRecipientFromUsers(List<User> users, Message message) {
        List<Recipient> recipients = new ArrayList<>();
        for (User user : users) {
            recipients.add(new Recipient(message, user));
        }

        recipients.forEach(RecipientService::addRecipient);
        return recipients;
    }
}
