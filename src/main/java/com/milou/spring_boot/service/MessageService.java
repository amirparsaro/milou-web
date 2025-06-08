package com.milou.spring_boot.service;

import com.milou.spring_boot.exception.MessageAlreadyExistsException;
import com.milou.spring_boot.exception.MessageNotFoundException;
import com.milou.spring_boot.model.Message;
import com.milou.spring_boot.model.Recipient;
import com.milou.spring_boot.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MessageService {
    private static SessionFactory sessionFactory;

    private static void setUpSessionFactory() {
        sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();
    }

    private static void closeSessionFactory() {
        sessionFactory.close();
    }

    public Message getMessage(Session session, Integer id) throws MessageNotFoundException {
        List<Message> allMessages = session.createNativeQuery("select * from messages where id = :given_id", Message.class)
                .setParameter("given_id", id)
                .getResultList();

        if (allMessages.isEmpty()) {
            throw new MessageNotFoundException(id);
        }

        return allMessages.getFirst();
    }

    public Message getMessage(Integer id) throws MessageNotFoundException {
        setUpSessionFactory();

        Message message = sessionFactory.fromTransaction(session -> {
            try {
                return getMessage(session, id);
            } catch (MessageNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        closeSessionFactory();

        return message;
    }

    public Message getMessage(String code) throws MessageNotFoundException {
        setUpSessionFactory();

        Message message = sessionFactory.fromTransaction(session -> {
            try {
                int id = Integer.parseInt(code.toLowerCase(), 36);
                return getMessage(session, id);
            } catch (MessageNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        closeSessionFactory();

        return message;
    }

    public void deleteMessage(Integer id) throws MessageNotFoundException {
        setUpSessionFactory();
        sessionFactory.inTransaction(session -> {
            try {
                Message message = getMessage(session, id);
            } catch (MessageNotFoundException e) {
                throw new RuntimeException(e);
            }

            session.createNativeMutationQuery("delete from messages " +
                            "where id = :given_id")
                    .setParameter("given_id", id)
                    .executeUpdate();
        });
        closeSessionFactory();
    }

    public void addMessage(Message message) throws MessageAlreadyExistsException {
        setUpSessionFactory();
        AtomicBoolean messageFound = new AtomicBoolean(true);
        sessionFactory.inTransaction(session -> {
            try {
                Message messageFromDb = getMessage(session, message.getId());
            } catch (MessageNotFoundException e) {
                messageFound.set(false);
            }

            if (messageFound.get()) {
                throw new MessageAlreadyExistsException(message.getId());
            }

            session.createNativeMutationQuery(
                            "insert into messages (code, date, title, body, sender_id, replied_to_id, forwarded_from_id) " +
                                    "values (:code, :date, :title, :body, :senderId, :repliedToId, :forwardedFromId)")
                    .setParameter("code", message.getCode())
                    .setParameter("date", message.getDate())
                    .setParameter("title", message.getTitle())
                    .setParameter("body", message.getBody())
                    .setParameter("senderId", message.getSender().getId())
                    .setParameter("repliedToId",
                            message.getRepliedTo() != null ? message.getRepliedTo().getId() : null)
                    .setParameter("forwardedFromId",
                            message.getForwardedFrom() != null ? message.getForwardedFrom().getId() : null)
                    .executeUpdate();
        });
        closeSessionFactory();
    }

    public void updateMessage(Message message) throws MessageNotFoundException {
        setUpSessionFactory();

        sessionFactory.inTransaction(session -> {
            try {
                Message existingMessage = getMessage(session, message.getId());
            } catch (MessageNotFoundException e) {
                throw new RuntimeException(e);
            }

            session.createNativeMutationQuery(
                            "update messages " +
                                    "set code = :code," +
                                    "date = :date," +
                                    "title = :title, " +
                                    "body = :body, " +
                                    "sender_id = :senderId, " +
                                    "replied_to_id = :repliedToId, " +
                                    "forwarded_from_id = :forwardedFromId " +
                                    "where id = :id")
                    .setParameter("code", message.getCode())
                    .setParameter("date", message.getDate())
                    .setParameter("title", message.getTitle())
                    .setParameter("body", message.getBody())
                    .setParameter("senderId", message.getSender().getId())
                    .setParameter("repliedToId", message.getRepliedTo() != null ? message.getRepliedTo().getId() : null)
                    .setParameter("forwardedFromId", message.getForwardedFrom() != null ? message.getForwardedFrom().getId() : null)
                    .setParameter("id", message.getId())
                    .executeUpdate();
        });
    }
}
