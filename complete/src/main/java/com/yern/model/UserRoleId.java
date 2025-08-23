package com.yern.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name="user_roles")
public class UserRole {
    @JoinColumn
    @Setter
    @ManyToOne(
            fetch = FetchType.LAZY
    )
    private User user;

    @JoinColumn
    @Setter
    @ManyToOne(
            fetch = FetchType.LAZY
    )
    private Role role;
}
