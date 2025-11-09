package com.yern.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.yern.model.common.AuditTimestamp;
import com.yern.model.user.User;

import java.time.LocalDateTime;
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
                new AuditTimestamp(
                    LocalDateTime.now(),
                    LocalDateTime.now()
                )
        );

        user.setId(123L);
    }

    @Test
    public void setUpdatedAtOptionalWithValue() {
        LocalDateTime currTime = LocalDateTime.now();
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();

        assertNotEquals(currTime, originalUpdatedAt);

        user.setUpdatedAt(Optional.of(currTime));
        assertEquals(currTime, user.getUpdatedAt());
    }

    @Test
    public void setUpdatedAtEmptyOptional() {
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();

        user.setUpdatedAt(Optional.empty());
        assertTrue(
            originalUpdatedAt.isBefore(user.getUpdatedAt())
        );
    }

    @Test
    public void equals_returnsTrue_whenSameObject() {
        assertEquals(user, user);
    }

    @Test
    public void equals_returnsFalse_whenObjectNotInstanceOfUserClass() {
        assertNotEquals("", user);
    }

    @Test
    public void equals_returnsTrue_whenIdsAreEqual() {
        User otherUser = new User();
        otherUser.setId(user.getId());

        assertEquals(user, otherUser);
    }

    @Test
    public void equals_returnsTrue_whenEmailsAreEqual() {
        User otherUser = new User();
        otherUser.setEmail(user.getEmail());

        assertEquals(user, otherUser);
    }

    @Test
    public void hashcode_returnsAHash_ofTheUsersIdHashCode() {
        int hashCode =  user.hashCode();
        assertEquals(hashCode, user.hashCode());

        User otherUser = new User();
        otherUser.setId(user.getId());

        int otherHashCode = otherUser.hashCode();
        assertEquals(otherHashCode, hashCode);

        otherUser.setId(user.getId() + 1L);
        assertNotEquals(otherUser.hashCode(), hashCode);
    }
}

