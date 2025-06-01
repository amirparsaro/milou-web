package com.milou.spring_boot.model;

import jakarta.persistence.*;

import java.util.ArrayList;

@Entity
@Table(name="users")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Basic(optional = false)
    private String title;

    @Basic(optional = false)
    private String body;

    private ArrayList<String> participants;

    public Message(String title, String body, ArrayList<String> participants) {
        this.title = title;
        this.body = body;
        this.participants = participants;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<String> participants) {
        this.participants = participants;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getId() {
        return id;
    }
}
