package com.yern.service.user;

import com.yern.dto.user.UserPostDto;
import com.yern.exceptions.DuplicateException;
import com.yern.exceptions.NotFoundException;
import com.yern.model.user.User;
import com.yern.repository.user.UserRepository;

import io.jsonwebtoken.lang.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserService() {}

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.getUserById(id);
    }

    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    public User createUser(User user) {
       return userRepository.save(user);
    }

    public void validateUserForRegistration(UserPostDto user) throws DuplicateException {
        User foundUser = this.getUserByEmail(user.getEmail());

        if (foundUser != null) {
            throw new DuplicateException(String.format(
                "User with the email address '%s' already exists.",
                user.getEmail())
            );
        }
    } 

    // TODO: add unit tests
    public void validateUser(Long id) throws NotFoundException {        
        String message = "User " + id + " is not valid";
        if (id == null) {
            throw new NotFoundException(message);
        }

        User foundUser = getUserById(id);
        if (foundUser == null) {
            throw new NotFoundException(message);
        }
    }
}