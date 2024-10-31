package com.leerv.diary.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiaryTitleDto {
    private Long id;
    private String title;
}
