package com.yern.model.storage;

import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "file_access_controls")
@Getter
@Setter
@NoArgsConstructor
public class FileAccessControl {

    // TODO: add unit test
    public static FileAccessControl from(
        Long userId, 
        Long fileId, 
        Long roleId
    ) {
        FileAccessControl accessControl = new FileAccessControl();
        accessControl.setUserId(userId);
        accessControl.setFileId(fileId);
        accessControl.setRoleId(roleId);

        return accessControl;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column 
    private Long userId;

    @Column 
    private Long roleId;

    @Column 
    private Long fileId;

    @Column 
    private LocalDateTime createdAt;

    @Column 
    private LocalDateTime updatedAt;

    @PrePersist 
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
            updatedAt = createdAt;
        }

        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate 
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; 
        if (obj == null || getClass() != obj.getClass()) return false;

        FileAccessControl other = (FileAccessControl) obj; 

        return (
            this.roleId == other.getRoleId() && 
            this.fileId == other.getFileId() && 
            this.userId == other.getUserId()
        );
    }

    @Override
    public int hashCode() {
        return (
            31 * (
                Long.hashCode(roleId) +
                Long.hashCode(fileId) + 
                Long.hashCode(userId)
            )
        );
    }
}
