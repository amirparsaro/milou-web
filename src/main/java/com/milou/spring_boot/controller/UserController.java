package com.milou.spring_boot.controller;

import java.util.List;

import com.milou.spring_boot.exception.UserNotFoundException;
import com.milou.spring_boot.model.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/Users")
    public List<User> all() {
        return null;
    }

    @PostMapping("/Users")
    public User newUser(@RequestBody User newUser) {
        return null;
    }

    @GetMapping("/Users/{id}")
    public User one(@PathVariable Long id) throws UserNotFoundException {

        return null;
    }

    @PutMapping("/Users/{id}")
    public User replaceUser(@RequestBody User newUser, @PathVariable Long id) {

        return null;
    }

    @DeleteMapping("/Users/{id}")
    public void deleteUser(@PathVariable Long id) {

    }
}