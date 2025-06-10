package com.milou.spring_boot.controller;

import java.util.List;

import com.milou.spring_boot.exception.MessageNotFoundException;
import com.milou.spring_boot.model.Message;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    @GetMapping("/Messages")
    public List<Message> all() {
        return null;
    }

    @PostMapping("/Messages")
    public Message newMessage(@RequestBody Message newMessage) {
        return null;
    }

    @GetMapping("/Messages/{id}")
    public Message one(@PathVariable Long id) throws MessageNotFoundException {

        return null;
    }

    @PutMapping("/Messages/{id}")
    public Message replaceMessage(@RequestBody Message newMessage, @PathVariable Long id) {

        return null;
    }

    @DeleteMapping("/Messages/{id}")
    public void deleteMessage(@PathVariable Long id) {
    }
}