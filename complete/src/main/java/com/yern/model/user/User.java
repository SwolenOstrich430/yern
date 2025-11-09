package com.yern.model.user;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.cglib.core.Local;

import com.yern.model.common.AuditTimestamp;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name="users")
@Getter
@Setter
public class User {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    @Column
	private String firstName;

    @Basic
    @Column
	private String lastName;

    @Basic
    @Column
    private String email;

	@Basic 
	@Column(name = "audit_timestamps") 
    private AuditTimestamp auditTimestamps;

	public User(
		String firstName, 
		String lastName,
		String email,
		AuditTimestamp auditTimestamps
	) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.auditTimestamps = auditTimestamps;
	}

    public User(
        String firstName,
        String lastName,
        String email
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.auditTimestamps = new AuditTimestamp();
		this.auditTimestamps.setCreatedAt(LocalDateTime.now());
		this.auditTimestamps.setCreatedAt(
			this.auditTimestamps.getCreatedAt()
		);
    }

    public User() {}

	public void setUpdatedAt(Optional<LocalDateTime> updatedAt) {
		this.auditTimestamps.setUpdatedAt(updatedAt.orElse(LocalDateTime.now()));
	}

	public LocalDateTime getCreatedAt() {
		return getAuditTimestamps().getCreatedAt();
	}

	public LocalDateTime getUpdatedAt() {
		return getAuditTimestamps().getUpdatedAt();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof User)) return false;

		User User = (User) o;

		return (
			Objects.equals(this.id, User.id) || 
			Objects.equals(this.email, User.email)
		);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	@Override
	public String toString() {
		return "User{" + "id=" + this.id + ", email='" + this.email + '}';
	}
}