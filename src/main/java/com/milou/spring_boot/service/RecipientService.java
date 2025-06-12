package com.milou.spring_boot.service;

import com.milou.spring_boot.SessionFactoryManager;
import com.milou.spring_boot.exception.MessageNotFoundException;
import com.milou.spring_boot.exception.RecipientAlreadyExistsException;
import com.milou.spring_boot.exception.RecipientNotFoundException;
import com.milou.spring_boot.exception.UserNotFoundException;
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
    public static Recipient getRecipient(Session session, Integer id) throws RecipientNotFoundException {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();
        List<Recipient> allRecipients = session.createNativeQuery("select * from recipients where id = :given_id", Recipient.class)
                .setParameter("given_id", id)
                .getResultList();

        if (allRecipients.isEmpty()) {
            throw new RecipientNotFoundException(id);
        }

        return allRecipients.getFirst();
    }

    public static Recipient getRecipientByUserIdMessageCode(Integer userId, String messageCode) throws RecipientNotFoundException, MessageNotFoundException {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();
        int messageId = MessageService.getMessageByCode(messageCode).getId();
        List<Recipient> allRecipients = sessionFactory.fromTransaction(session ->
                session.createNativeQuery("select * from recipients where recipient_id = :user_id and message_id = :message_id", Recipient.class)
                        .setParameter("user_id", userId)
                        .setParameter("message_id", messageId)
                        .getResultList());

        if (allRecipients.isEmpty()) {
            throw new RecipientNotFoundException();
        }

        return allRecipients.getFirst();
    }

    public static Recipient getRecipient(Integer id) throws RecipientNotFoundException {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();
        Recipient recipient = sessionFactory.fromTransaction(session -> {
            try {
                return getRecipient(session, id);
            } catch (RecipientNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        return recipient;
    }

    public static void deleteRecipient(Integer id) throws RecipientNotFoundException {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();
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
    }

    public static void addRecipient(Recipient recipient, Session session) throws RecipientAlreadyExistsException {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();
        session.persist(recipient);
        session.flush();

    }

    public static void updateRecipient(Recipient recipient) throws RecipientNotFoundException {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();
        sessionFactory.inTransaction(session -> {
            try {
                Recipient existingRecipient = getRecipient(session, recipient.getId());
            } catch (RecipientNotFoundException e) {
                throw new RuntimeException(e);
            }

            session.createNativeMutationQuery("update recipients " +
                            "set message_id = :message_id, recipient_id = :recipient_id, is_read = :is_read " +
                            "where id = :id")
                    .setParameter("message_id", recipient.getMessage().getId())
                    .setParameter("recipient_id", recipient.getRecipient().getId())
                    .setParameter("id", recipient.getId())
                    .setParameter("is_read", recipient.isRead())
                    .executeUpdate();
        });
    }

    public static Recipient createRecipientFromUser(User user, Message message, Session session) {
        Recipient recipient = new Recipient(message, user);
        addRecipient(recipient, session);
        return recipient;
    }

    public static List<Recipient> createRecipientFromUsers(List<User> users, Message message, Session session) {
        List<Recipient> recipients = new ArrayList<>();
        for (User user : users) {
            recipients.add(new Recipient(message, user));
        }

        for (Recipient recipient : recipients) {
            addRecipient(recipient, session);
        }
        return recipients;
    }
}
