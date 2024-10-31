package com.leerv.diary.controllers;

import com.leerv.diary.dto.RegistrationDto;
import com.leerv.diary.dto.LoginDto;
import com.leerv.diary.services.AuthenticationService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthenticationController {
    @Autowired
    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegistrationDto request
    ) throws MessagingException {
        System.out.println("in controller");
        service.register(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto request) {
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> jwtTokens = service.login(request);
        String refreshTokenCookie = String.format(
                "refresh_token=%s; Max-Age=%s; Path=%s; HttpOnly",
                jwtTokens.get("refreshToken"), 30 * 24 * 60 * 60, "/" //TODO: change path for cookie
        );
        headers.set("Authorization", jwtTokens.get("accessToken"));
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie);
        return ResponseEntity.ok().headers(headers).body("Authenticated");
    }

    @GetMapping("/resend-activation-code")
    public void resendActivationCode(
            @RequestParam String email
    ) throws MessagingException {
        service.resendActivationCode(email);
    }

    @GetMapping("/activate-account")
    public void activateAccount(
            @RequestParam String activationCode
    ) {
        service.activateAccount(activationCode);
    }

    @GetMapping("/update-tokens")
    public ResponseEntity<?> updateTokens(
            @CookieValue(name = "refresh_token") String refreshToken
    ) {
        Map<String, String> jwtTokens = service.updateTokens(refreshToken);
        String refreshTokenCookie = String.format(
                "refresh_token=%s; Max-Age=%s; Path=%s; HttpOnly",
                jwtTokens.get("refreshToken"), 30 * 24 * 60 * 60, "/"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtTokens.get("accessToken"));
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie);
        return ResponseEntity.ok().headers(headers).body("Authenticated");
    }
}
