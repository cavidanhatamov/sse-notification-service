package com.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple response DTO containing only the template ID
 * Used for template creation responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateIdResponseDTO {

    private String id;

    /**
     * Create response DTO from template ID
     */
    public static TemplateIdResponseDTO of(String id) {
        return new TemplateIdResponseDTO(id);
    }
}
