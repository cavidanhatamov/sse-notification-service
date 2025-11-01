package com.learn.service;

import com.learn.dto.TemplateRequestDTO;
import com.learn.dto.TemplateResponseDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service for managing notification templates.
 */
public interface TemplateService {

    /**
     * Retrieves all templates.
     *
     * @return Flux of template DTOs
     */
    Flux<TemplateResponseDTO> getAllTemplates();

    /**
     * Retrieves only active templates.
     *
     * @return Flux of active template DTOs
     */
    Flux<TemplateResponseDTO> getActiveTemplates();

    /**
     * Retrieves a template by ID.
     *
     * @param templateId the template identifier
     * @return Mono with template DTO
     */
    Mono<TemplateResponseDTO> getTemplateById(String templateId);

    /**
     * Searches templates by name (case-insensitive).
     *
     * @param templateName the template name to search
     * @return Flux of matching template DTOs
     */
    Flux<TemplateResponseDTO> getTemplatesByName(String templateName);

    /**
     * Creates a new template.
     *
     * @param templateRequest the template creation request
     * @return Mono with the created template ID
     */
    Mono<String> createTemplate(TemplateRequestDTO templateRequest);

    /**
     * Updates an existing template.
     *
     * @param templateId      the template identifier
     * @param templateRequest the template update request
     * @return Mono with updated template DTO
     */
    Mono<TemplateResponseDTO> updateTemplate(String templateId, TemplateRequestDTO templateRequest);
}
