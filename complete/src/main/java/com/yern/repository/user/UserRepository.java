package com.yern.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.yern.model.user.User;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    User getUserById(Long id);
    User getUserByEmail(String email);
    User getByEmail(String email);
    Optional<User> findByEmail(String email);
}