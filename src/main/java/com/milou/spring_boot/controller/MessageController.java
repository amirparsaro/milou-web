package com.milou.spring_boot.controller;

import java.util.ArrayList;
import java.util.List;

import com.milou.spring_boot.exception.MessageAlreadyExistsException;
import com.milou.spring_boot.exception.MessageNotFoundException;
import com.milou.spring_boot.exception.RecipientNotFoundException;
import com.milou.spring_boot.exception.UserNotFoundException;
import com.milou.spring_boot.model.Message;
import com.milou.spring_boot.model.Recipient;
import com.milou.spring_boot.model.User;
import com.milou.spring_boot.service.AuthService;
import com.milou.spring_boot.service.MessageService;
import com.milou.spring_boot.service.RecipientService;
import com.milou.spring_boot.service.UserService;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @GetMapping("/get/all")
    public ResponseEntity<Object> getAllMessages(@RequestHeader("Authorization") String token) {
        if (!AuthService.isUserLogged(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not logged in.");
        }

        User user = AuthService.getUserFromToken(token);
        List<Message> userMessages = MessageService.getAllMessages(user);
        return ResponseEntity.ok(userMessages);
    }

    @GetMapping("/get/unread")
    public ResponseEntity<Object> getUnreadMessages(@RequestHeader("Authorization") String token) {
        if (!AuthService.isUserLogged(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not logged in.");
        }

        User user = AuthService.getUserFromToken(token);
        List<Message> userMessages = MessageService.getUnreadReceivedMessages(user);
        return ResponseEntity.ok(userMessages);
    }

    @GetMapping("/get/sent")
    public ResponseEntity<Object> getSentMessages(@RequestHeader("Authorization") String token) {
        if (!AuthService.isUserLogged(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not logged in.");
        }

        User user = AuthService.getUserFromToken(token);
        List<Message> userMessages = MessageService.getAllSentMessages(user);
        return ResponseEntity.ok(userMessages);
    }

    @GetMapping("/get/by-code")
    public ResponseEntity<Object> getMessageByCode(@RequestHeader("Authorization") String token, @RequestParam("code") String code) {
        if (!AuthService.isUserLogged(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not logged in.");
        }

        User user = AuthService.getUserFromToken(token);
        Message userMessage = null;
        try {
            userMessage = MessageService.getMessageByCode(code);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message does not exist, or is not accessed by user.");
        }

        if (userMessage.getSender().getId().equals(user.getId()))
            return ResponseEntity.ok(userMessage);
        else {
            List<Recipient> messageRecipients = userMessage.getRecipients();
            for (Recipient recipient : messageRecipients) {
                if (recipient.getRecipient().getId().equals(user.getId()))
                    return ResponseEntity.ok(userMessage);
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message does not exist, or is not accessed by user.");
    }

    @GetMapping("/create/message")
    public ResponseEntity<String> createMessage(
            @RequestHeader("Authorization") String token,
            @RequestParam("recipientEmails") List<String> recipientEmails,
            @RequestParam("title") String title,
            @RequestParam("body") String body) {

        User sender = AuthService.getUserFromToken(token);
        ArrayList<User> recipients = new ArrayList<>();
        for (String recipientEmail : recipientEmails) {
            User recipient = null;
            try {
                recipient = UserService.getUserByEmail(recipientEmail);
            } catch (UserNotFoundException e) {
                return ResponseEntity.badRequest().body("Recipient with email: " + recipientEmail + " not found");
            }

            if (recipient == null) {
                return ResponseEntity.badRequest().body("Recipient with email: " + recipientEmail + " not found");
            }
            recipients.add(recipient);
        }

        try {
            String messageCode = MessageService.createMessage(sender, recipients, title, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(messageCode);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating message: " + e.getMessage());
        }
    }

    @GetMapping("/create/reply")
    public ResponseEntity<String> createReplyToMessage(
            @RequestHeader("Authorization") String token,
            @RequestParam("messageCode") String messageCode,
            @RequestParam("body") String body) {

        if (messageCode == null || messageCode.isEmpty()) {
            return ResponseEntity.badRequest().body("Message code cannot be empty");
        }
        if (body == null || body.isEmpty()) {
            return ResponseEntity.badRequest().body("Body cannot be empty");
        }

        User sender = AuthService.getUserFromToken(token);
        if (sender == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }

        try {
            String newMessageCode = MessageService.createReplyToMessage(sender, messageCode, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(newMessageCode);
        } catch (MessageNotFoundException e) {
            return ResponseEntity.badRequest().body("Original message not found: " + messageCode);
        } catch (MessageAlreadyExistsException e) {
            return ResponseEntity.badRequest().body("Reply already exists for message: " + messageCode);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating reply message: " + e.getMessage());
        }
    }

    @GetMapping("/create/forward")
    public ResponseEntity<String> createForwardedMessage(
            @RequestHeader("Authorization") String token,
            @RequestParam("recipientEmails") List<String> recipientEmails,
            @RequestParam("messageCode") String messageCode) {

        if (messageCode == null || messageCode.isEmpty()) {
            return ResponseEntity.badRequest().body("Message code cannot be empty");
        }
        if (recipientEmails == null || recipientEmails.isEmpty()) {
            return ResponseEntity.badRequest().body("At least one recipient is required");
        }

        User sender = AuthService.getUserFromToken(token);
        if (sender == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }

        ArrayList<User> recipients = new ArrayList<>();
        for (String recipientEmail : recipientEmails) {
            User recipient = null;
            try {
                recipient = UserService.getUserByEmail(recipientEmail);
            } catch (UserNotFoundException e) {
                return ResponseEntity.badRequest().body("Recipient with email: " + recipientEmail + " not found");
            }
            if (recipient == null) {
                return ResponseEntity.badRequest().body("Recipient with email: " + recipientEmail + " not found");
            }
            recipients.add(recipient);
        }

        try {
            String newMessageCode = MessageService.createForwardedMessage(sender, recipients, messageCode);
            return ResponseEntity.status(HttpStatus.CREATED).body(newMessageCode);
        } catch (MessageNotFoundException e) {
            return ResponseEntity.badRequest().body("Original message not found: " + messageCode);
        } catch (MessageAlreadyExistsException e) {
            return ResponseEntity.badRequest().body("This message has already been forwarded.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating message: " + e.getMessage());
        }
    }

    @GetMapping("/markAsRead")
    public ResponseEntity<String> markAsRead(@RequestHeader("Authorization") String token, @RequestParam String messageCode) {
        User reader = AuthService.getUserFromToken(token);

        if (reader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }

        if (messageCode == null || messageCode.isEmpty()) {
            return ResponseEntity.badRequest().body("Message code cannot be empty");
        }

        int readerId = reader.getId();
        try {
            Recipient recipient = RecipientService.getRecipientByUserIdMessageCode(readerId, messageCode);
            recipient.setRead(true);
            RecipientService.updateRecipient(recipient);
            return ResponseEntity.ok("Marked as read for recipient.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
