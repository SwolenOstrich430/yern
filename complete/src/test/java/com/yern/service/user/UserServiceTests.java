package com.yern.service.user;

import com.yern.dto.user.UserPostDto;
import com.yern.exceptions.DuplicateException;
import com.yern.model.user.User;
import com.yern.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = UserService.class)
public class UserServiceTests {
    @MockitoBean
    private UserRepository userRepository;
    private UserService userService;
    private String email;

    @BeforeEach()
    public void setup() {
        this.email = UUID.randomUUID().toString();
        this.userService = new UserService(userRepository);
    }

    @Test
    public void validateUserForRegistration_throwsDuplicateException_whenUserAlreadyExists() {
        User user = mock(User.class);
        UserPostDto userPostDto = new UserPostDto();
        userPostDto.setEmail(email);

        when(userRepository.getUserByEmail(email)).thenReturn(user);

        assertThrows(DuplicateException.class, () -> {
            userService.validateUserForRegistration(userPostDto);
        });
    }

    @Test
    public void validateUserForRegistration_doesntThrowDuplicateException_whenUserDoesntExist() {
        UserPostDto userPostDto = new UserPostDto();
        userPostDto.setEmail(email);

        when(userRepository.getUserByEmail(email)).thenReturn(null);
        userService.validateUserForRegistration(userPostDto);
    }

}
