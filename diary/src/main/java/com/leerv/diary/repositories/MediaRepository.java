package com.leerv.diary.repositories;

import com.leerv.diary.entities.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {
    @Query("""
            select media
            from Media media
            where media.diary is null
            """)
    List<Media> findAllOrphans();
}
