package se.product_service_1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class JwtTestController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok("Hello, anonymous!");
        }

        // Cast Authentication to JwtAuthenticationToken
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        Jwt jwt = jwtAuth.getToken();

        String username = jwt.getSubject();      // "sub" claim
        Long id = jwt.getClaim("id");            // custom "id" claim

        return ResponseEntity.ok("Hello, " + username + "! Your ID is " + id);
    }
}
