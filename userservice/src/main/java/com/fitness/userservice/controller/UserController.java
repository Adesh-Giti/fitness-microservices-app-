package com.fitness.userservice.controller;


import com.fitness.userservice.dto.RequestDTO;
import com.fitness.userservice.dto.ResponseDto;
import com.fitness.userservice.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseDto> getUserById(@PathVariable String userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDto> register(@Valid @RequestBody RequestDTO requestDTO){
        return ResponseEntity.ok(userService.register(requestDTO));
    }

    @GetMapping("/{userId}/validate")
    public ResponseEntity<Boolean> validate(@PathVariable String userId){
        return ResponseEntity.ok(userService.userExitById(userId));
    }


}
