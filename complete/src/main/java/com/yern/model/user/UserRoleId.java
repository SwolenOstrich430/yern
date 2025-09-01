//package com.yern.model.user;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.io.Serializable;
//
//@Getter
//@Setter
//public class UserRoleId implements Serializable {
//    @JoinColumn
//    @ManyToOne(
//            fetch = FetchType.LAZY
//    )
//    private Long userId;
//
//    @JoinColumn
//    @ManyToOne(
//            fetch = FetchType.LAZY
//    )
//    private Long roleId;
//
//    public UserRoleId(User user, Role role) {
//        this.userId = user.getId();
//        this.roleId = role.getId();
//    }
//
//    public UserRoleId(Long userId, Long roleId) {
//        this.userId = userId;
//        this.roleId = roleId;
//    }
//}
