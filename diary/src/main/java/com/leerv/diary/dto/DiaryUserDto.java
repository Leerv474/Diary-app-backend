package com.leerv.diary.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiaryUserDto {
    private Long diaryId;
    private Long userId;
}
