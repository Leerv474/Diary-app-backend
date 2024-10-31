package com.leerv.diary.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "media")
public class Media {
    @Id
    @GeneratedValue
    private Long id;
    private String filePath;
    private Long identifierNumber;
    private boolean isArchived;
    private String mediaType;

    @ManyToOne
    @JoinColumn(name = "diary_id", referencedColumnName = "id")
    private Diary diary;

}
