package com.yern.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.yern.model.User;

import java.time.Instant;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes={User.class})
public class UserTests {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(
                "Peter",
                "Connelly",
                "pconnelly898@gmail.com",
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    public void setUpdatedAtOptionalWithValue() {
        Instant currTime = Instant.now();
        Instant originalUpdatedAt = user.getUpdatedAt();

        assertNotEquals(currTime, originalUpdatedAt);

        user.setUpdatedAt(Optional.of(currTime));
        assertEquals(currTime, user.getUpdatedAt());
    }

    @Test
    public void setUpdatedAtOptionalWithNullValue() {
        Instant originalUpdatedAt = user.getUpdatedAt();

        user.setUpdatedAt(Optional.empty());
        assertTrue(
            originalUpdatedAt.isBefore(user.getUpdatedAt())
        );
    }
}

