package com.yern.controller;

import com.yern.model.UserRole;
import com.yern.service.UserRoleService;
import com.yern.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.yern.model.User;


@RestController
@RequestMapping("/api/users/roles")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @PostMapping
    public ResponseEntity<UserRole> createUserRole(@RequestBody UserRole userRole) {
        return ResponseEntity.ok(userRoleService.createRole(userRole));
    }
}
