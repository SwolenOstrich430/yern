package com.yern.controller.user;

import com.yern.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.yern.dto.security.authentication.UserDetailsImpl;
import com.yern.model.user.User;

import java.nio.file.AccessDeniedException;
import java.util.Optional;


@RestController
@RequestMapping("${api.users-endpoint}")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Optional<User>> getUserByEmail(
        @AuthenticationPrincipal UserDetailsImpl currentUser,
        @RequestParam(required = true) String email
    ) {
        // Currently, don't allow anyone to search anyone else 
        // This will change in the future when we do friends 
        // Let people think they're actually searching if they're 
        // doing this for email discovery.
        if (!currentUser.getUsername().equals(email)) {
            return ResponseEntity.of(Optional.empty());
        }

        return ResponseEntity.ok(
            Optional.ofNullable(userService.getUserByEmail(email))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<User>> getUser(
        @AuthenticationPrincipal UserDetailsImpl currentUser,
        @PathVariable Long id
    ) {
        if (currentUser.getUserId() != id) {
            return ResponseEntity.of(Optional.empty());
        }

        return ResponseEntity.ok(
            Optional.ofNullable(userService.getUserById(id))
        );
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(
        @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        if (userDetails == null) {
            throw new AccessDeniedException("No user found,");
        }

        return ResponseEntity.of(
            Optional.of(userService.getUserByEmail(userDetails.getUsername())
        ));
    }
}
