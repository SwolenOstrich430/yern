package com.yern.model;

import java.time.Instant;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.*;


@Entity
@Table(name="users")
public class User {

	private @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    @SequenceGenerator(name = "users_seq", sequenceName = "users_seq", allocationSize = 1)
    Long id;
	private String firstName;
	private String lastName;
	private String email;
	private Instant createdAt;
	private Instant updatedAt;

	public User(
		String firstName, 
		String lastName,
		String email,
		Instant createdAt,
		Instant updatedAt
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
        this.createdAt = Instant.now() ;
        this.updatedAt = Instant.now() ;
    }

    public User() {
        this.createdAt = Instant.now() ;
        this.updatedAt = Instant.now() ;
    }

    public Long getId() {
		return this.id;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public String getEmail() {
		return this.email;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public Instant getUpdatedAt() {
		return this.updatedAt;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setUpdatedAt(Optional<Instant> updatedAt) {
		this.updatedAt = updatedAt.orElse(Instant.now() );
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