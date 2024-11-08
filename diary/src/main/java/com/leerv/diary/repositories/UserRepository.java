package com.leerv.diary.repositories;

import com.leerv.diary.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("""
            select user
            from User user
            where user.accountLocked = true
            """)
    List<User> findAllLocked();
}
