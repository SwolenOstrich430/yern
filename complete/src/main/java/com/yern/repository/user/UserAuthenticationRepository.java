package com.yern.repository.user;

import com.yern.model.user.UserAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthenticationRepository extends JpaRepository<UserAuthentication,Long> {
    UserAuthentication getByUserId(Long userId);
//    UserAuthentication createUserAuthentication(UserAuthenticationPostDto userAuthentication);
}
