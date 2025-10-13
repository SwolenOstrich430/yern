package com.yern.model.pattern;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO: eventually, this will need to take permissions into account
@Entity
@Table(name = "users_patterns")
@Getter
@Setter
@NoArgsConstructor
@IdClass(UserPatternId.class)
public class UserPattern implements Serializable {
    @Id
    @Column
    private Long patternId;
    @Id
    @Column
    private Long userId;
}