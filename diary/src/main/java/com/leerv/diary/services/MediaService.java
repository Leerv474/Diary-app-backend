package com.leerv.diary.services;

import com.leerv.diary.dto.MediaUploadDto;
import com.leerv.diary.entities.Media;
import com.leerv.diary.exception.DiaryException;
import com.leerv.diary.exception.MediaException;
import com.leerv.diary.repositories.DiaryRepository;
import com.leerv.diary.repositories.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {
    private final MediaRepository mediaRepository;
    private final DiaryRepository diaryRepository;
    @Value("${application.storage.upload-directory}")
    private String uploadDirectory;

    public Long upload(MultipartFile file, MediaUploadDto request) {
        if (file.isEmpty()) {
            throw new MediaException("File is empty");
        }
        try {
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown_file";
            Path filePath = uploadPath.resolve(UUID.randomUUID() + "_" + originalFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            Media media = Media.builder()
                    .isArchived(false)
                    .filePath(filePath.toString())
                    .identifierNumber(request.getIdentifierNumber())
                    .mediaType(request.getMediaType())
                    .diary(diaryRepository.findById(request.getDiaryId())
                            .orElseThrow(() -> new DiaryException("Diary not found")))
                    .build();
            return media.getId();
        } catch (IOException e) {
            throw new MediaException("Upload failed");
        }
    }
}
