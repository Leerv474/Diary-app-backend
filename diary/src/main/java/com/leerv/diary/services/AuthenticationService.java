package com.leerv.diary.services;

import com.leerv.diary.dto.LoginDto;
import com.leerv.diary.dto.RegistrationDto;
import com.leerv.diary.entities.ActivationCode;
import com.leerv.diary.entities.User;
import com.leerv.diary.exception.AuthenticationException;
import com.leerv.diary.repositories.ActivationCodeRepository;
import com.leerv.diary.repositories.RefreshTokenRepository;
import com.leerv.diary.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final EmailValidationService emailValidationService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ActivationCodeRepository activationCodeRepository;

    @Transactional
    public void register(@Valid RegistrationDto request) throws MessagingException {
        System.out.println("in service");
        User user = User.builder()
                .accountEnabled(false)
                .accountLocked(false)
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);
        emailValidationService.sendValidationEmail(user);
    }

    @Transactional
    public Map<String, String> login(@Valid LoginDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = (User) authentication.getPrincipal();
        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);
        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    @Transactional
    public Map<String, String> updateTokens(String extractedRefreshToken) {
        String username = jwtTokenService.extractUsername(extractedRefreshToken);
        if (!jwtTokenService.refreshTokenMatches(username, extractedRefreshToken)) {
            throw new AuthenticationException("Refresh token does not match or has been revoked");
        }
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("user not found"));
        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);
        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    @Transactional
    public void activateAccount(String activationCode) {
        ActivationCode code = activationCodeRepository.findByCode(activationCode).orElseThrow(() -> new AuthenticationException("Activation code not found"));
        if (LocalDateTime.now().isAfter(code.getExpiresAt())) {
            throw new AuthenticationException("Activation token has expired");
        }
        User user = userRepository.findById(code.getUser().getId()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setAccountEnabled(true);
        userRepository.save(user);
        code.setValidatedAt(LocalDateTime.now());
        activationCodeRepository.save(code);
    }

    public void resendActivationCode(String email) throws MessagingException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        emailValidationService.sendValidationEmail(user);
    }
}
