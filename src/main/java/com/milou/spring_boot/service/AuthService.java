package com.milou.spring_boot.service;

import com.milou.spring_boot.exception.InvalidCredentialsException;
import com.milou.spring_boot.model.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

@Service
public class AuthService {
    private static HashMap<String, User> tokens = new HashMap<>();

    public static String logIn(String email, String password) throws InvalidCredentialsException {
        User user = UserService.logIn(email, password);
        String token = UUID.randomUUID().toString();

        tokens.put(token, user);
        return token;
    }

    public static boolean isUserLogged(String token) {
        return tokens.containsKey(token);
    }
}
