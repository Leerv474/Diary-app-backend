package com.leerv.diary.controllers;

import com.leerv.diary.dto.*;
import com.leerv.diary.services.DiaryService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("diary")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping("/create")
    public ResponseEntity<Long> createDiary(
            @Valid @RequestBody DiaryCreateDto request,
            Authentication connectedUser
    ) {
        String title = request.getTitle();
        Long archiveStorageTime = request.getArchiveStorageTime();
        return ResponseEntity.ok(diaryService.createDiary(title, archiveStorageTime, connectedUser));
    }

    @GetMapping("/delete")
    public ResponseEntity<?> createDiary(
            @RequestParam Long diaryId,
            Authentication connectedUser
    ) {
        diaryService.deleteDiary(diaryId, connectedUser);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/edit_title")
    public ResponseEntity<?> editTitle(
            @Valid @RequestBody DiaryTitleDto request,
            Authentication connectedUser
    ) {
        diaryService.editTitle(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/edit_content")
    public ResponseEntity<?> editContent(
            @Valid @RequestBody DiaryDto request,
            Authentication connectedUser
    ) {
        diaryService.editContent(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add_user")
    public ResponseEntity<?> addUser(
            @Valid @RequestBody DiaryUserDto request,
            Authentication connectedUser
    ) {
        diaryService.addUser(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("remove_user")
    public ResponseEntity<?> removeUser(
            @Valid @RequestBody DiaryUserDto request,
            Authentication connectedUser
    ) {
        diaryService.removeUser(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/archive")
    public ResponseEntity<Long> archiveDiary(
            @RequestParam Long diaryId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(diaryService.archive(diaryId, connectedUser));
    }

    @GetMapping("/unarchive")
    public ResponseEntity<Long> unarchiveDiary(
            @RequestParam Long diaryId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(diaryService.unarchive(diaryId, connectedUser));
    }

    @PostMapping("/add_tag")
    public ResponseEntity<?> addTag(
            @Valid @RequestBody DiaryTagDto request,
            Authentication connectedUser
    ) {
        diaryService.addTag(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/remove_tag")
    public ResponseEntity<?> removeTag(
            @Valid @RequestBody DiaryTagDto request,
            Authentication connectedUser
    ) {
        diaryService.removeTag(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/find")
    public ResponseEntity<List<DiaryListDto>> findDiary(
            @RequestParam String title,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(diaryService.findByTitle(title, connectedUser));
    }

    @GetMapping("/list")
    public ResponseEntity<List<DiaryListDto>> listDiaries(
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(diaryService.listAll(connectedUser));
    }
}
