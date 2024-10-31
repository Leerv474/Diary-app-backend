package com.leerv.diary.services;

import com.leerv.diary.dto.*;
import com.leerv.diary.entities.Diary;
import com.leerv.diary.entities.Media;
import com.leerv.diary.entities.User;
import com.leerv.diary.exception.DiaryException;
import com.leerv.diary.repositories.DiaryRepository;
import com.leerv.diary.repositories.MediaRepository;
import com.leerv.diary.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;

    public Long createDiary(String title, Long archiveStorageTime, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Diary newDiary = Diary.builder()
                .title(title)
                .content("")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isArchived(false)
                .archiveStorageTime(archiveStorageTime)
                .users(List.of(user))
                .build();
        Diary savedDiary = diaryRepository.save(newDiary);
        return savedDiary.getId();
    }

    public void deleteDiary(Long diaryId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException("Diary not found"));
        boolean isOwner = diary.getUsers().stream().anyMatch(owner -> owner.getId().equals(user.getId()));
        if (!isOwner) {
            throw new DiaryException("You do not have the rights to delete this diary");
        }
        diaryRepository.delete(diary);
    }

    public void editTitle(DiaryTitleDto dto, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Diary diary = diaryRepository.findById(dto.getId()).orElseThrow(() -> new DiaryException("Diary not found"));
        boolean isOwner = diary.getUsers().stream().anyMatch(owner -> owner.getId().equals(user.getId()));
        if (!isOwner) {
            throw new DiaryException("You do not have rights to edit this diary");
        }
        if (diary.isArchived()) {
            throw new DiaryException("Diary is archived");
        }
        diary.setTitle(dto.getTitle());
        diaryRepository.save(diary);
    }

    public void editContent(DiaryDto request, Authentication connectedUser) {
        Diary diary = diaryRepository.findById(request.getId()).orElseThrow(() -> new DiaryException("Diary not found"));
        User user = (User) connectedUser.getPrincipal();
        boolean isOwner = diary.getUsers().stream().anyMatch(owner -> owner.getId().equals(user.getId()));
        if (!isOwner) {
            throw new DiaryException("You do not have rights to edit this diary");
        }
        if (diary.isArchived()) {
            throw new DiaryException("Diary is archived");
        }
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());
        List<Long> mediaIds = request.getMediaIdList();
        if (mediaIds != null) {
            List<Media> newMedia = mediaRepository.findAllById(request.getMediaIdList());
            diary.setMediaList(newMedia);
        }
        diary.setUpdatedAt(LocalDateTime.now());
        diaryRepository.save(diary);
    }

    public void addUser(DiaryUserDto request, Authentication connectedUser) {
        Diary diary = diaryRepository.findById(request.getDiaryId())
                .orElseThrow(() -> new DiaryException("Diary not found"));
        User user = (User) connectedUser.getPrincipal();
        boolean isOwner = diary.getUsers().stream().anyMatch(owner -> owner.getId().equals(user.getId()));
        if (!isOwner) {
            throw new DiaryException("You do not have the right to add users to edit this diary");
        }
        if (diary.isArchived()) {
            throw new DiaryException("Diary is archived");
        }
        User newEditor = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("Editor not found"));
        diary.getUsers().add(newEditor);
        diaryRepository.save(diary);
    }

    public void removeUser(DiaryUserDto request, Authentication connectedUser) {
        Diary diary = diaryRepository.findById(request.getDiaryId())
                .orElseThrow(() -> new DiaryException("Diary not found"));
        User user = (User) connectedUser.getPrincipal();
        boolean isOwner = diary.getUsers().stream().anyMatch(owner -> owner.getId().equals(user.getId()));
        if (!isOwner) {
            throw new DiaryException("You do not have the right to add users to edit this diary");
        }
        if (diary.isArchived()) {
            throw new DiaryException("Diary is archived");
        }
        User editor = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("Editor not found"));
        if (editor.getEmail().equals(user.getEmail())) {
            throw new DiaryException("You cannot remove yourself from editors group");
        }
        diary.getUsers().remove(editor);
        diaryRepository.save(diary);
    }

    public void addTag(DiaryTagDto request, Authentication connectedUser) {
        Diary diary = diaryRepository.findById(request.getDiaryId())
                .orElseThrow(() -> new DiaryException("Diary not found"));
        User user = (User) connectedUser.getPrincipal();
        boolean isOwner = diary.getUsers().stream().anyMatch(owner -> owner.getId().equals(user.getId()));
        if (!isOwner) {
            throw new DiaryException(("You do not have the rights to edit this diary"));
        }
        if (diary.isArchived()) {
            throw new DiaryException("Diary is archived");
        }
        String tag = request.getTag();
        if (diary.getTags() == null) {
            diary.setTags(new HashSet<>());
        }
        diary.getTags().add(tag);
        diaryRepository.save(diary);
    }

    public Long archive(Long diaryId, Authentication connectedUser) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException("Diary not found"));
        User user = (User) connectedUser.getPrincipal();
        boolean isOwner = diary.getUsers().stream().anyMatch(owner -> owner.getId().equals(user.getId()));
        if (!isOwner) {
            throw new DiaryException("You do not have the rights to archive this diary");
        }
        if (diary.isArchived()) {
            throw new DiaryException("Diary is archived already");
        }
        diary.setArchived(true);
        diary.setArchivedAt(LocalDate.now());
        diaryRepository.save(diary);
        return diary.getId();
    }

    public Long unarchive(Long diaryId, Authentication connectedUser) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryException("Diary not found"));
        User user = (User) connectedUser.getPrincipal();
        boolean isOwner = diary.getUsers().stream().anyMatch(owner -> owner.getId().equals(user.getId()));
        if (!isOwner) {
            throw new DiaryException("You do not have the rights to archive this diary");
        }
        if (!diary.isArchived()) {
            throw new DiaryException("This diary wasn't archived");
        }
        diary.setArchived(false);
        diary.setArchivedAt(null);
        diaryRepository.save(diary);
        return diary.getId();
    }

    public List<DiaryListDto> findByTitle(String title, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        List<Diary> diaryList = diaryRepository.findByTitleAndUser(title, user.getId());
        return diaryList.stream()
                .map(diary -> DiaryListDto.builder()
                        .id(diary.getId())
                        .title(diary.getTitle())
                        .isArchived(diary.isArchived())
                        .build()).toList();
    }

    public List<DiaryListDto> listAll(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        List<Diary> diaryList = diaryRepository.findAllByUser(user.getId());
        return diaryList.stream()
                .map(diary -> DiaryListDto.builder()
                        .id(diary.getId())
                        .title(diary.getTitle())
                        .isArchived(diary.isArchived())
                        .build()).toList();
    }

    public void removeTag(@Valid DiaryTagDto request, Authentication connectedUser) {
        Diary diary = diaryRepository.findById(request.getDiaryId())
                .orElseThrow(() -> new DiaryException("Diary not found"));
        User user = (User) connectedUser.getPrincipal();
        boolean isOwner = diary.getUsers().stream().anyMatch(owner -> owner.getId().equals(user.getId()));
        if (!isOwner) {
            throw new DiaryException("You do not have the rights to archive this diary");
        }
        diary.getTags().remove(request.getTag());
        diaryRepository.save(diary);
    }
}
