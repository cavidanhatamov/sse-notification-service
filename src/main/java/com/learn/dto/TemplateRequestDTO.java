package com.learn.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequestDTO {

    private String id; // Optional - will be auto-generated if not provided

    @NotBlank(message = "Template name is required")
    private String name;

    @NotBlank(message = "Template channel is required")
    private String channel;

    private boolean active = true; // Optional - defaults to true

    @Valid
    private List<TemplateParamDTO> params; // Optional - template parameters

    @NotEmpty(message = "Template translations are required")
    @Valid
    private Map<String, TranslationDTO> translations;

    private String createdBy; // Optional - who created this template

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateParamDTO {
        @NotBlank(message = "Parameter key is required")
        private String key;

        @NotBlank(message = "Parameter type is required")
        private String type;

        private boolean required;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranslationDTO {
        @NotBlank(message = "Translation subject is required")
        private String subject;

        @NotBlank(message = "Translation content is required")
        private String content;
    }
}
