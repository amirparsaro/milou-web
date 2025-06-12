package com.milou.spring_boot.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = true)
    private String code;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Basic(optional = false)
    private String title;

    @Basic(optional = false)
    private String body;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Recipient> recipients;

    @ManyToOne
    @JoinColumn(name = "replied_to_id")
    private Message repliedTo;

    @ManyToOne
    @JoinColumn(name = "forwarded_from_id")
    private Message forwardedFrom;

    public Message() {
    }

    public Message(User sender, String title, String body, ArrayList<Recipient> recipients, Message repliedTo, Message forwardedFrom) {
        this.sender = sender;
        this.title = title;
        this.body = body;
        this.recipients = recipients;
        this.setDate(LocalDate.now());
        if (repliedTo != null)
            this.repliedTo = repliedTo;
        if (forwardedFrom != null)
            this.forwardedFrom = forwardedFrom;
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

    public void setCode() {
        this.code = String.format("%6s", Integer.toString(this.id, 36))
                .replace(' ', '0').toUpperCase();
    }

    public String getCode() {
        return code;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<Recipient> recipients) {
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
