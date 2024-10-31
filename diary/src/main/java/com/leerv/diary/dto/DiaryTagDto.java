package com.leerv.diary.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiaryTagDto {
    private Long diaryId;
    private String tag;
}