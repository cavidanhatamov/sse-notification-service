package com.learn.service.impl;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.learn.dto.TemplateRequestDTO;
import com.learn.dto.TemplateResponseDTO;
import com.learn.exception.TemplateNotFoundException;
import com.learn.model.Template;
import com.learn.repository.TemplateRepository;
import com.learn.service.TemplateService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation of template management service.
 */
@Service
public class TemplateServiceImpl implements TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateServiceImpl.class);
    private final TemplateRepository templateRepository;

    public TemplateServiceImpl(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public Flux<TemplateResponseDTO> getAllTemplates() {
        log.debug("Fetching all templates");
        return templateRepository.findAll()
                .map(TemplateResponseDTO::fromTemplate);
    }

    @Override
    public Flux<TemplateResponseDTO> getActiveTemplates() {
        log.debug("Fetching active templates");
        return templateRepository.findByActiveTrue()
                .map(TemplateResponseDTO::fromTemplate);
    }

    @Override
    public Mono<TemplateResponseDTO> getTemplateById(String templateId) {
        log.debug("Fetching template by ID: {}", templateId);
        return templateRepository.findById(templateId)
                .map(TemplateResponseDTO::fromTemplate)
                .switchIfEmpty(Mono.defer(() -> 
                    Mono.error(new TemplateNotFoundException(templateId))));
    }

    @Override
    public Flux<TemplateResponseDTO> getTemplatesByName(String templateName) {
        log.debug("Fetching templates by name: {}", templateName);
        return templateRepository.findByName(templateName)
                .map(TemplateResponseDTO::fromTemplate);
    }

    @Override
    public Mono<String> createTemplate(TemplateRequestDTO templateRequest) {
        if (templateRequest.getId() == null || templateRequest.getId().isEmpty()) {
            templateRequest.setId(java.util.UUID.randomUUID().toString());
        }

        Template template = mapToEntity(templateRequest);

        return templateRepository.save(template)
                .doOnSuccess(saved -> log.debug("Created template: {} with ID: {}", saved.getName(), saved.getId()))
                .map(Template::getId);
    }

    @Override
    @SuppressWarnings("unused")
    public Mono<TemplateResponseDTO> updateTemplate(String templateId, TemplateRequestDTO templateRequest) {
        return templateRepository.findById(templateId)
                .switchIfEmpty(Mono.defer(() -> 
                    Mono.error(new TemplateNotFoundException(templateId))))
                .map(existingTemplate -> {
                    templateRequest.setId(templateId);
                    Template updatedTemplate = mapToEntity(templateRequest);

                    if (existingTemplate.getMeta() != null) {
                        updatedTemplate.getMeta().setCreatedBy(existingTemplate.getMeta().getCreatedBy());
                        updatedTemplate.getMeta().setCreatedAt(existingTemplate.getMeta().getCreatedAt());
                    }
                    updatedTemplate.getMeta().setUpdatedAt(Instant.now());

                    return updatedTemplate;
                })
                .flatMap(templateRepository::save)
                .map(TemplateResponseDTO::fromTemplate)
                .doOnSuccess(updated -> log.debug("Updated template: {}", templateId));
    }

    private Template mapToEntity(TemplateRequestDTO dto) {
        Template template = new Template();
        template.setId(dto.getId());
        template.setName(dto.getName());
        template.setChannel(dto.getChannel());
        template.setActive(dto.isActive());

        if (dto.getParams() != null) {
            template.setParams(dto.getParams().stream()
                    .map(paramDto -> new Template.TemplateParam(
                            paramDto.getKey(),
                            paramDto.getType(),
                            paramDto.isRequired(),
                            paramDto.getDescription()))
                    .collect(Collectors.toList()));
        }

        if (dto.getTranslations() != null) {
            template.setTranslations(dto.getTranslations().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> new Template.Translation(
                                    entry.getValue().getSubject(),
                                    entry.getValue().getContent()))));
        }

        Template.TemplateMeta meta = new Template.TemplateMeta();
        meta.setCreatedBy(dto.getCreatedBy());
        meta.setCreatedAt(Instant.now());
        meta.setUpdatedAt(Instant.now());
        template.setMeta(meta);

        return template;
    }
}
