package com.yern.service;

import com.yern.model.User;
import com.yern.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.getUserById(id);
    }

    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }
}