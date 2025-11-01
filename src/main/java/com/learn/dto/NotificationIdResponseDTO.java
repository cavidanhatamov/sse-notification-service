package com.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple response DTO containing only the notification ID
 * Used for notification send responses in JSON format
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationIdResponseDTO {

    private String id;

    /**
     * Create response DTO from notification ID
     */
    public static NotificationIdResponseDTO of(String id) {
        return new NotificationIdResponseDTO(id);
    }
}
