package com.fitness.userservice.dto;


import com.fitness.userservice.models.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
public class ResponseDto {
    private String id;
    private String email;
    private String password;
    private String firstName;
    private String LastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
