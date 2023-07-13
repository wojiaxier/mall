package com.hbwxz.user.repo;

import com.hbwxz.user.pojo.Oauth2Client;
import com.hbwxz.user.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    User findByUserName(String userName);
    User findByUserPhone(String userPhone);

    @Override
    Optional<User> findById(Integer id);
}
