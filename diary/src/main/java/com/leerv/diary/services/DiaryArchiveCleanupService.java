package com.leerv.diary.services;

import com.leerv.diary.entities.Diary;
import com.leerv.diary.repositories.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryArchiveCleanupService {
    private final DiaryRepository diaryRepository;

    @Scheduled(cron = "0 0 * * * *") // Runs every hour
    public void archiveDiaries() {
        List<Diary> diariesToArchive = diaryRepository.findAllArchived();
        for (Diary diary : diariesToArchive) {
            if (diary.getArchivedAt() == null) {
                continue;
            }
            LocalDate archiveExpiration = diary.getArchivedAt().plusDays(diary.getArchiveStorageTime());
            boolean archiveExpired = archiveExpiration.isAfter(LocalDate.now());
            if (diary.isArchived() && archiveExpired) {
                diaryRepository.delete(diary);
            }
        }
        System.out.println("Archived diaries processed successfully.");
    }
}
