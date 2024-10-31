package com.leerv.diary.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "Users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User implements UserDetails, Principal {
    @Id
    @GeneratedValue
    private Long id;
    @Size(min = 2, max = 128)
    @Column(nullable = false)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @NotNull
    private String password;
    private boolean accountEnabled;
    private boolean accountLocked;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<RefreshToken> refreshTokens;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<ActivationCode> activationCodes;
    @ManyToMany(mappedBy = "users")
    private List<Diary> diaries;

    //==============================================================//

    @Override
    public String getName() {
        return this.email;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.accountLocked;
    }

    @Override
    public boolean isEnabled() {
        return this.accountEnabled;
    }

}
