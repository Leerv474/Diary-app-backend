package com.leerv.diary.repositories;

import com.leerv.diary.entities.Diary;
import com.leerv.diary.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    @Query("""
            SELECT diary
            FROM Diary diary
            JOIN diary.users user
            WHERE diary.title LIKE %:title%
            AND user.id = :userId
            """)
    List<Diary> findByTitleAndUser(String title, Long userId);

    @Query("""
            SELECT diary
            FROM Diary diary
            JOIN diary.users user
            WHERE user.id = :userId
            """)
    List<Diary> findAllByUser(Long userId);

    @Query("""
            select diary
            from Diary diary
            where diary.isArchived = true
            """)
    List<Diary> findAllArchived();
}
