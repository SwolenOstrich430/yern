package com.yern.model.user;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name="users")
@Getter
@Setter
public class User {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    @SequenceGenerator(name = "users_seq", sequenceName = "users_seq", allocationSize = 1)
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
    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Basic
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

	public User(
		String firstName, 
		String lastName,
		String email,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

    public User(
        String firstName,
        String lastName,
        String email
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User() {}

	public void setUpdatedAt(Optional<LocalDateTime> updatedAt) {
		this.updatedAt = updatedAt.orElse(LocalDateTime.now() );
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