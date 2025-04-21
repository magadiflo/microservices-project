package dev.magadiflo.gateway.server.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class AppController {

    @GetMapping(path = "/authorized")
    public ResponseEntity<Map<String, String>> authorized(@RequestParam String code) {
        return ResponseEntity.ok(Collections.singletonMap("code", code));
    }

    @PostMapping(path = "/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Collections.singletonMap("logout", "OK"));
    }
}
