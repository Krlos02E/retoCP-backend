package com.cinetest.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private int status;
    private LocalDateTime timestamp;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .timestamp(LocalDateTime.now())
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> success(int status, String message) {
        return success(status, message, null);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .status(status)
                .timestamp(LocalDateTime.now())
                .message(message)
                .build();
    }
}
