package com.learn.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {

    @NotBlank(message = "Template ID is required")
    private String templateId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Channel is required")
    private String channel;
    private String priority; // Optional - defaults to "NORMAL"
    private String sourceSystem; // Optional
    private Map<String, Object> params; // Optional - template parameters
    private Map<String, Object> metadata; // Optional - dynamic metadata
}
