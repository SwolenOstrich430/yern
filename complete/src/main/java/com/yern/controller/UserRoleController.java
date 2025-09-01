//package com.yern.controller;
//
//import com.yern.model.user.UserRole;
//import com.yern.service.UserRoleService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//
//@RestController
//@RequestMapping("${api.user-roles-endpoint}")
//public class UserRoleController {
//
//    @Autowired
//    private UserRoleService userRoleService;
//
//    @PostMapping
//    public ResponseEntity<UserRole> createUserRole(@RequestBody UserRole userRole) {
//        return ResponseEntity.ok(userRoleService.createRole(userRole));
//    }
//}
