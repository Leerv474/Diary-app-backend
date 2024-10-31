package com.leerv.diary.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leerv.diary.dto.MediaUploadDto;
import com.leerv.diary.exception.MediaException;
import com.leerv.diary.services.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("media")
public class MediaController {
    private final MediaService service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> uploadMediaFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("json") String json
    ) throws JsonProcessingException {
        MediaUploadDto request = new ObjectMapper().readValue(json, MediaUploadDto.class);
        return ResponseEntity.ok(service.upload(file, request));
    }
}
