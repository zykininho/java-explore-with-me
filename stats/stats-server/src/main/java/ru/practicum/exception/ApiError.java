package ru.practicum.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ApiError {

    private List<String> errors;
    private String message;
    private String reason;
    private HttpStatus status;
    private LocalDateTime timestamp;

}