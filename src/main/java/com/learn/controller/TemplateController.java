package com.learn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learn.dto.TemplateIdResponseDTO;
import com.learn.dto.TemplateRequestDTO;
import com.learn.dto.TemplateResponseDTO;
import com.learn.service.TemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST API for template management.
 */
@Tag(name = "Templates", description = "Notification template management APIs")
@RestController
@RequestMapping("/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * Get all templates
     */
    @Operation(summary = "Get all templates", description = "Retrieves all notification templates from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved templates", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TemplateResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public Flux<TemplateResponseDTO> getAllTemplates() {
        return templateService.getAllTemplates();
    }

    /**
     * Get active templates only
     */
    @Operation(summary = "Get active templates", description = "Retrieves only active templates that can be used for notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active templates"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/active")
    public Flux<TemplateResponseDTO> getActiveTemplates() {
        return templateService.getActiveTemplates();
    }

    /**
     * Get template by ID
     * Returns 400 Bad Request if template not found
     */
    @Operation(summary = "Get template by ID", description = "Retrieves a specific template by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved template"),
            @ApiResponse(responseCode = "400", description = "Template not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{templateId}")
    public Mono<TemplateResponseDTO> getTemplateById(
            @Parameter(description = "Template ID", required = true, example = "welcome-email") @PathVariable String templateId) {
        return templateService.getTemplateById(templateId);
    }

    /**
     * Get templates by name
     */
    @Operation(summary = "Search templates by name", description = "Search templates by name (case-insensitive partial match)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching templates"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/name/{templateName}")
    public Flux<TemplateResponseDTO> getTemplatesByName(
            @Parameter(description = "Template name to search", required = true, example = "welcome") @PathVariable String templateName) {
        return templateService.getTemplatesByName(templateName);
    }

    /**
     * Create a new template
     */
    @Operation(summary = "Create a new template", description = "Creates a new notification template with multi-language support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template created successfully", content = @Content(schema = @Schema(implementation = TemplateIdResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public Mono<ResponseEntity<TemplateIdResponseDTO>> createTemplate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Template creation request with translations", required = true, content = @Content(schema = @Schema(implementation = TemplateRequestDTO.class))) @Valid @RequestBody TemplateRequestDTO templateRequest) {
        return templateService.createTemplate(templateRequest)
                .map(TemplateIdResponseDTO::of)
                .map(ResponseEntity::ok);
    }

    /**
     * Update an existing template
     */
    @Operation(summary = "Update an existing template", description = "Updates an existing template's content and translations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template updated successfully"),
            @ApiResponse(responseCode = "400", description = "Template not found or invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{templateId}")
    public Mono<TemplateResponseDTO> updateTemplate(
            @Parameter(description = "Template ID to update", required = true, example = "welcome-email") @PathVariable String templateId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Template update request", required = true) @Valid @RequestBody TemplateRequestDTO templateRequest) {
        return templateService.updateTemplate(templateId, templateRequest);
    }
}
