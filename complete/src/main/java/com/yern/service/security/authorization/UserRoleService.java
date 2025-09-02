//package com.yern.service;
//
//import com.yern.model.user.UserRole;
//import com.yern.repository.UserRoleRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class UserRoleService {
//
//    @Autowired
//    private UserRoleRepository userRoleRepository;
//
//    public List<UserRole> findByUserId(Long userId) {
//        return userRoleRepository.findUserRoleByUserId(userId);
//    }
//
//    public UserRole createRole(UserRole userRole) {
//        return userRoleRepository.save(userRole);
//    }
//
//    public UserRole createRole(Long userId, Long roleId) {
//        UserRole userRole = new UserRole(userId, roleId);
//        return createRole(userRole);
//    }
//}