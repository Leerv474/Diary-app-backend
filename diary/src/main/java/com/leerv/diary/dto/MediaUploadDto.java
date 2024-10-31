package com.leerv.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediaUploadDto {
    private Long diaryId;
    private Long identifierNumber;
    private String mediaType;
}
