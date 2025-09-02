package com.yern.service.user;

import com.yern.dto.user.UserGetDto;
import com.yern.dto.user.UserPostDto;
import com.yern.exceptions.DuplicateException;
import com.yern.mapper.UserMapper;
import com.yern.model.user.User;
import com.yern.repository.user.UserRepository;
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

    public UserGetDto getUserGetDto(Long id) {
        User user = this.getUserById(id);
        return UserMapper.modelToDto(user);
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
}