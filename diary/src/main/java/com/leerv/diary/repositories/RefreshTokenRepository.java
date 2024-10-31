package com.leerv.diary.repositories;

import com.leerv.diary.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Query("""
            select rs
            from  RefreshToken rs
            where rs.user.email = :email
            """)
    List<RefreshToken> findByEmail(@Param("email") String email);
    @Query("""
            select rs
            from RefreshToken rs
            where rs.user.email = :email
            and rs.sessionId = :sessionId
            """)
    Optional<RefreshToken> findByEmailAndSessionId(@Param("email") String email, @Param("sessionId") String sessionId);
}
