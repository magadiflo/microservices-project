package dev.magadiflo.user.app.controller;

import dev.magadiflo.user.app.model.dto.UserEnabledRequest;
import dev.magadiflo.user.app.model.dto.UserRequest;
import dev.magadiflo.user.app.model.dto.UserResponse;
import dev.magadiflo.user.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> findUsers() {
        return ResponseEntity.ok(this.userService.findUsers());
    }

    @GetMapping(path = "/{userId}")
    public ResponseEntity<UserResponse> findUser(@PathVariable Long userId) {
        return ResponseEntity.ok(this.userService.findUser(userId));
    }

    @GetMapping(path = "/username/{username}")
    public ResponseEntity<UserResponse> findUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(this.userService.findUserByUsername(username));
    }

    @PostMapping
    public ResponseEntity<UserResponse> saveUser(@RequestBody UserRequest userRequest) {
        UserResponse userResponse = this.userService.saveUser(userRequest);
        URI location = URI.create("/api/v1/users/%d".formatted(userResponse.id()));
        return ResponseEntity.created(location).body(userResponse);
    }

    @PutMapping(path = "/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(this.userService.updateUser(userId, userRequest));
    }

    @DeleteMapping(path = "/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        this.userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/{userId}/enabled")
    public ResponseEntity<UserResponse> updateUserEnabled(@PathVariable Long userId, @RequestBody UserEnabledRequest userEnabledRequest) {
        return ResponseEntity.ok(this.userService.updateUserEnabled(userId, userEnabledRequest));
    }

    @PatchMapping(path = "/{userId}/roles")
    public ResponseEntity<UserResponse> updateUserRoles(@PathVariable Long userId, @RequestBody Set<String> roleNames) {
        return ResponseEntity.ok(this.userService.updateUserRoles(userId, roleNames));
    }
}
