package com.milou.spring_boot.controller;

import com.milou.spring_boot.exception.InvalidCredentialsException;
import com.milou.spring_boot.exception.InvalidRegistrationException;
import com.milou.spring_boot.exception.UserNotFoundException;
import com.milou.spring_boot.model.User;
import com.milou.spring_boot.service.AuthService;
import com.milou.spring_boot.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/log-in")
    public static ResponseEntity<String> logIn(@RequestParam String email,
                                               @RequestParam String password) {
        User user;
        try {
            user = UserService.logIn(email, password);
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        String token = AuthService.storeUserToken(user);
        return ResponseEntity.status(HttpStatus.FOUND).body(token);
    }

    @GetMapping("/sign-up")
    public static ResponseEntity<String> signUp(@RequestParam String name,
                                                @RequestParam String email,
                                                @RequestParam String password) {
        User user;
        try {
            user = UserService.signUp(name, email, password);
        } catch (InvalidRegistrationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        String token = AuthService.storeUserToken(user);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(token);
    }

    @GetMapping("/log-out")
    public static ResponseEntity<String> logOut(@RequestHeader("Authorization") String token) {
        try {
            UserService.logOut(token);
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(token);
    }
}