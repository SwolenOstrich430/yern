package com.yern.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.yern.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    User getUserById(Long id);
    User getUserByEmail(String email);
}