package com.leerv.diary.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;


@Entity
@Table(name = "Refresh_tokens")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue
    private Long id;
    @Column(length = 512, unique = true, nullable = false)
    private String token;
    private Date issuedAt;
    private Date expiresAt;
    private boolean revoked;
    private String sessionId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
