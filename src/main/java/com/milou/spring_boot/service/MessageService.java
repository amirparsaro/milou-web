package com.milou.spring_boot.service;

import com.milou.spring_boot.SessionFactoryManager;
import com.milou.spring_boot.exception.MessageAlreadyExistsException;
import com.milou.spring_boot.exception.MessageNotFoundException;
import com.milou.spring_boot.model.Message;
import com.milou.spring_boot.model.Recipient;
import com.milou.spring_boot.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MessageService {
    public static Message getMessage(Session session, Integer id) throws MessageNotFoundException {
        List<Message> allMessages = session.createNativeQuery("select * from messages where id = :given_id", Message.class)
                .setParameter("given_id", id)
                .getResultList();

        if (allMessages.isEmpty()) {
            throw new MessageNotFoundException(id);
        }

        return allMessages.getFirst();
    }

    public static Message getMessage(Integer id) throws MessageNotFoundException {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();

        Message message = sessionFactory.fromTransaction(session -> {
            try {
                return getMessage(session, id);
            } catch (MessageNotFoundException e) {
                throw new RuntimeException(e);
            }
        });


        return message;
    }

    public static Message getMessageByCode(String code) throws MessageNotFoundException, RuntimeException {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();

        Message message = sessionFactory.fromTransaction(session -> {
            try {
                int id = Integer.parseInt(code.toLowerCase(), 36);
                return getMessage(session, id);
            } catch (MessageNotFoundException e) {
                throw new RuntimeException(e);
            }
        });


        return message;
    }

    public static void deleteMessage(Integer id) throws MessageNotFoundException {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();
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
    }

    public static String addMessage(Message message, List<User> recipients) throws MessageAlreadyExistsException {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();

        sessionFactory.inTransaction(session -> {
            session.persist(message);
            session.flush();

            message.setCode();
            ArrayList<Recipient> recipientsArraylist = (ArrayList<Recipient>) RecipientService.createRecipientFromUsers(recipients, message, session);
            message.setRecipients(recipientsArraylist);
            session.merge(message);
            session.flush();

        });

        return message.getCode();
    }

    public static void updateMessage(Message message) throws MessageNotFoundException {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();

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

    public static String createMessage(User sender, List<User> recipients, String title, String body) {
        Message message = new Message(sender, title, body, null, null, null);

        return addMessage(message, recipients);
    }

    public static String createReplyToMessage(User sender, String messageCode, String body) throws MessageNotFoundException, MessageAlreadyExistsException {
        Message repliedTo = getMessageByCode(messageCode);
        Message message = new Message(sender, "[Re] " + repliedTo.getTitle(), body, null, repliedTo, repliedTo.getForwardedFrom());

        User messageSender = repliedTo.getSender();
        List<User> recipients = new ArrayList<>();
        recipients.add(messageSender);
        List<Recipient> recipientsForRepliedTo = repliedTo.getRecipients();
        for (Recipient recipient : recipientsForRepliedTo) {
            if (!recipient.getRecipient().getId().equals(sender.getId()))
                recipients.add(recipient.getRecipient());
        }

        return addMessage(message, recipients);
    }

    public static String createForwardedMessage(User sender, ArrayList<User> recipients, String messageCode) throws MessageNotFoundException, MessageAlreadyExistsException {
        Message forwardedFrom = getMessageByCode(messageCode);
        Message message = new Message(sender, "[Fw] " + forwardedFrom.getTitle(), forwardedFrom.getBody(), null, forwardedFrom.getRepliedTo(), forwardedFrom);

        return addMessage(message, recipients);
    }

    private static ArrayList<Message> getAllReceivedMessages(User receiver) {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();

        List<Message> allMessages = sessionFactory.fromTransaction(session ->
                session.createNativeQuery("select m.*\n" +
                                "from messages m\n" +
                                "join recipients r on m.id = r.message_id\n" +
                                "join users u on r.recipient_id = u.id\n" +
                                "where u.id = :user_id", Message.class)
                        .setParameter("user_id", receiver.getId())
                        .getResultList());

        return new ArrayList<>(allMessages);
    }

    public static ArrayList<Message> getAllSentMessages(User sender) {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();

        List<Message> allMessages = sessionFactory.fromTransaction(session ->
                session.createNativeQuery("select * from messages where sender_id = :given_id", Message.class)
                        .setParameter("given_id", sender.getId())
                        .getResultList());

        return new ArrayList<>(allMessages);
    }

    public static ArrayList<Message> getAllMessages(User user) {
        ArrayList<Message> sentMessages = getAllSentMessages(user);
        ArrayList<Message> receivedMessages = getAllReceivedMessages(user);

        ArrayList<Message> allMessages = new ArrayList<>(sentMessages);
        allMessages.addAll(receivedMessages);

        return allMessages;
    }

    public static ArrayList<Message> getUnreadReceivedMessages(User receiver) {
        SessionFactory sessionFactory = SessionFactoryManager.getSessionFactory();

        List<Message> allMessages = sessionFactory.fromTransaction(session ->
                session.createNativeQuery("select m.*\n" +
                                "from messages m\n" +
                                "join recipients r on m.id = r.message_id\n" +
                                "join users u on r.recipient_id = u.id\n" +
                                "where u.id = :user_id\n" +
                                "and r.is_read = false", Message.class)
                        .setParameter("user_id", receiver.getId())
                        .getResultList());

        return new ArrayList<>(allMessages);
    }
}
