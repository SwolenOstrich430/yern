package com.yern.model.storage;

import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;

import com.yern.model.common.AuditTimestamp;
import com.yern.model.security.authorization.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "file_access_control")
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

        Role role = new Role();
        role.setId(roleId);
        accessControl.setRole(role);

        return accessControl;
    }

    @Id 
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long Id; 
    
    @Column 
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "role_id")
    private Role role;

    @Column 
    private Long fileId;

    @Column 
    private AuditTimestamp auditTimestamps;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; 
        if (obj == null || getClass() != obj.getClass()) return false;

        FileAccessControl other = (FileAccessControl) obj; 

        return (
            this.getRole().getId() == other.getRole().getId() && 
            this.fileId == other.getFileId() && 
            this.userId == other.getUserId()
        );
    }

    @Override
    public int hashCode() {
        return (
            31 * (
                Long.hashCode(role.getId()) +
                Long.hashCode(fileId) + 
                Long.hashCode(userId)
            )
        );
    }
}
