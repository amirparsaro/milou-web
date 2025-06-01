package com.milou.spring_boot.model;

import jakarta.persistence.*;

import java.util.ArrayList;

@Entity
@Table(name="messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User sender;

    @Basic(optional = false)
    private String title;

    @Basic(optional = false)
    private String body;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private ArrayList<Recipient> recipients;

    @ManyToOne
    @JoinColumn(name = "replied_to_id")
    private Message repliedTo;

    @ManyToOne
    @JoinColumn(name = "forwarded_from_id")
    private Message forwardedFrom;

    public Message(User sender, String title, String body) {
        this.sender = sender;
        this.title = title;
        this.body = body;
    }

    public Integer getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public ArrayList<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(ArrayList<Recipient> recipients) {
        this.recipients = recipients;
    }

    public Message getRepliedTo() {
        return repliedTo;
    }

    public void setRepliedTo(Message repliedTo) {
        this.repliedTo = repliedTo;
    }

    public Message getForwardedFrom() {
        return forwardedFrom;
    }

    public void setForwardedFrom(Message forwardedFrom) {
        this.forwardedFrom = forwardedFrom;
    }
}
