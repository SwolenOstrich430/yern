package com.yern.controller.user;

import com.yern.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.yern.model.user.User;

import java.nio.file.AccessDeniedException;


@RestController
@RequestMapping("${api.users-endpoint}")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<User> getUserByEmail(@RequestParam(required = true) String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(
        @AuthenticationPrincipal UserDetails userDetails
    ) throws AccessDeniedException {
        if (userDetails == null) {
            throw new AccessDeniedException("No user found,");
        }

        return getUserByEmail(userDetails.getUsername());
    }
}
