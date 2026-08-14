package com.cinetest.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseDTO<T> {

    private int status;
    private LocalDateTime timestamp;
    private String message;
    private T data;

    public static <T> ApiResponseDTO<T> success(int status, String message, T data) {
        return ApiResponseDTO.<T>builder()
                .status(status)
                .timestamp(LocalDateTime.now())
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponseDTO<Void> success(int status, String message) {
        return success(status, message, null);
    }

    public static <T> ApiResponseDTO<T> error(int status, String message) {
        return ApiResponseDTO.<T>builder()
                .status(status)
                .timestamp(LocalDateTime.now())
                .message(message)
                .build();
    }
}
