package com.leerv.diary.services;

import com.leerv.diary.entities.Media;
import com.leerv.diary.repositories.MediaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaCleanupService {

    @Value("${application.storage.upload-directory}")
    private String uploadDirectory;
    @Value("${application.storage.file-retention-duration-hours}")
    private long fileRetentionDurationHours;
    private final MediaRepository mediaRepository;

    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void cleanUpFiles() {
        Path uploadPath = Paths.get(uploadDirectory);
        Instant cutoffTime = Instant.now().minus(fileRetentionDurationHours, ChronoUnit.HOURS);

        List<Media> orphanMedia = mediaRepository.findAllOrphans();
        List<String> orphanPaths = orphanMedia.stream().map(Media::getFilePath).toList();
        for (int i = 0; i < orphanPaths.size(); i++) {
            var path = Path.of(orphanPaths.get(i));
            if (isOlderThan(path, cutoffTime)) {
                deleteFile(path);
                mediaRepository.delete(orphanMedia.get(i));
            }
        }
    }

    private boolean isOlderThan(Path path, Instant cutoffTime) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            return attrs.creationTime().toInstant().isBefore(cutoffTime);
        } catch (IOException e) {
            System.err.println("Failed to check file age: " + e.getMessage());
            return false;
        }
    }

    private void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
            System.out.println("Deleted old file: " + path);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + path + " - " + e.getMessage());
        }
    }
}
