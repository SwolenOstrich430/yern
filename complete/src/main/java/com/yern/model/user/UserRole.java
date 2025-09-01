//package com.yern.model.user;
//
//import jakarta.persistence.EmbeddedId;
//import jakarta.persistence.Entity;
//
//@Entity
//public class UserRole {
//    @EmbeddedId
//    private UserRoleId userRoleId;
//
//    public UserRole(UserRoleId userRoleId) {
//        this.userRoleId = userRoleId;
//    }
//
//    public UserRole(User user, Role role) {
//        this.userRoleId = new UserRoleId(
//                user,
//                role
//        );
//    }
//
//    public UserRole(Long userId, Long roleId) {
//        this.userRoleId = new UserRoleId(
//                userId,
//                roleId
//        );
//    }
//
//    public UserRole() {}
//}
