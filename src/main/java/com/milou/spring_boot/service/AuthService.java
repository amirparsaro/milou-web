package com.milou.spring_boot.service;

import com.milou.spring_boot.exception.InvalidCredentialsException;
import com.milou.spring_boot.exception.UserNotFoundException;
import com.milou.spring_boot.model.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

@Service
public class AuthService {
    private static HashMap<String, User> tokens = new HashMap<>();

    public static String storeUserToken(User user) {
        String token = UUID.randomUUID().toString();

        tokens.put(token, user);
        return token;
    }

    public static boolean isUserLogged(String token) {
        return tokens.containsKey(token);
    }

    public static User getUserFromToken(String token) {
        return tokens.get(token);
    }

    public static void removeToken(String token) throws UserNotFoundException {
        if (!isUserLogged(token))
            throw new UserNotFoundException();

        tokens.remove(token);
    }
}
