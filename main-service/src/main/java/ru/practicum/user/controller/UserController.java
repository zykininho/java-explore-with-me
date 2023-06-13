package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAll(@RequestParam(required = false) List<Long> ids,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("Received GET-request at /admin/users endpoint");
        return ResponseEntity.ok().body(userService.getAll(ids, from, size));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody NewUserRequest newUserRequest) {
        log.info("Received POST-request at /admin/users endpoint with body {}", newUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(newUserRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable("id") Long userId) {
        log.info("Received DELETE-request at /admin/users/{} endpoint", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

}