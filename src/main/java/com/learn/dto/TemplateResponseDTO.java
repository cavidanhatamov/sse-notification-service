package com.learn.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.learn.model.Template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponseDTO {

    private String id;
    private String name;
    private String channel;
    private boolean active;
    private List<TemplateParamDTO> params;
    private Map<String, TranslationDTO> translations;
    private TemplateMetaDTO meta;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateParamDTO {
        private String key;
        private String type;
        private boolean required;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranslationDTO {
        private String subject;
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateMetaDTO {
        private String createdBy;
        private Instant createdAt;
        private Instant updatedAt;
    }

    /**
     * Convert Template entity to TemplateResponseDTO
     */
    public static TemplateResponseDTO fromTemplate(Template template) {
        if (template == null) {
            return null;
        }

        TemplateResponseDTO dto = new TemplateResponseDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setChannel(template.getChannel());
        dto.setActive(template.isActive());

        // Convert params
        if (template.getParams() != null) {
            dto.setParams(template.getParams().stream()
                    .map(param -> new TemplateParamDTO(
                            param.getKey(),
                            param.getType(),
                            param.isRequired(),
                            param.getDescription()))
                    .toList());
        }

        // Convert translations
        if (template.getTranslations() != null) {
            dto.setTranslations(template.getTranslations().entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> new TranslationDTO(
                                    entry.getValue().getSubject(),
                                    entry.getValue().getContent()))));
        }

        // Convert meta
        if (template.getMeta() != null) {
            dto.setMeta(new TemplateMetaDTO(
                    template.getMeta().getCreatedBy(),
                    template.getMeta().getCreatedAt(),
                    template.getMeta().getUpdatedAt()));
        }

        return dto;
    }
}
