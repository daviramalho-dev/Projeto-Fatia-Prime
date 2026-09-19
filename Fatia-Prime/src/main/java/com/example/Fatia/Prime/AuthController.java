package com.example.Fatia.Prime;

import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/csrf")
    public Map<String, String> csrfToken(CsrfToken token) {
        return Map.of("token", token.getToken());
    }
}
