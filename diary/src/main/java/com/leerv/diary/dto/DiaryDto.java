package com.leerv.diary.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DiaryDto {
    private Long id;
    private String title;
    private String content;
    private Long archiveStorageTime;
    private boolean isArchived;
    private List<Long> mediaIdList;
}
