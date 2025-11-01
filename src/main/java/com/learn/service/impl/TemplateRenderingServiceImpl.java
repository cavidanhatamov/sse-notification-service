package com.learn.service.impl;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.learn.exception.TemplateNotFoundException;
import com.learn.model.Notification;
import com.learn.model.Notification.RenderedContent;
import com.learn.model.Template;
import com.learn.repository.TemplateRepository;
import com.learn.service.TemplateRenderingService;

import reactor.core.publisher.Mono;

/**
 * Implementation of template rendering with multi-language support.
 */
@Service
public class TemplateRenderingServiceImpl implements TemplateRenderingService {

    private static final Logger log = LoggerFactory.getLogger(TemplateRenderingServiceImpl.class);
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    private final TemplateRepository templateRepository;

    public TemplateRenderingServiceImpl(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public Mono<Notification> renderNotification(Notification notification) {
        if (notification.getTemplateId() == null) {
            log.warn("Notification {} has no templateId, skipping rendering", notification.getId());
            return Mono.just(notification);
        }

        return templateRepository.findById(notification.getTemplateId())
                .cast(Template.class)
                .doOnNext(template -> log.debug("Found template {} for notification {}",
                        template.getName(), notification.getId()))
                .map(template -> renderNotificationInAllLanguages(notification, template))
                .doOnNext(rendered -> log.debug("Rendered notification {} in {} languages",
                        rendered.getId(),
                        rendered.getRenderedContent() != null ? rendered.getRenderedContent().size() : 0))
                .switchIfEmpty(Mono.defer(() -> 
                    Mono.error(new TemplateNotFoundException(notification.getTemplateId()))));
    }

    private Notification renderNotificationInAllLanguages(Notification notification, Template template) {
        // Set channel from template if not provided
        if (notification.getChannel() == null && template.getChannel() != null && !template.getChannel().isEmpty()) {
            notification.setChannel(template.getChannel());
        }

        // Render content in ALL languages
        java.util.Map<String, RenderedContent> renderedContentMap = new java.util.HashMap<>();

        if (template.getTranslations() != null && !template.getTranslations().isEmpty()) {
            template.getTranslations().forEach((lang, translation) -> {
                String renderedSubject = renderText(translation.getSubject(), notification.getParams());
                String renderedContent = renderText(translation.getContent(), notification.getParams());

                renderedContentMap.put(lang,
                        new RenderedContent(renderedSubject, renderedContent));

                log.debug("Rendered notification {} in language: {}", notification.getId(), lang);
            });
        } else {
            log.warn("No translations found for template {}", template.getId());
        }

        notification.setRenderedContent(renderedContentMap);

        // Keep backward compatibility - set subject/content from Azerbaijani (default language)
        RenderedContent defaultContent = renderedContentMap.get("az");
        if (defaultContent == null && !renderedContentMap.isEmpty()) {
            // Fallback to any available language if "az" is not present
            defaultContent = renderedContentMap.values().iterator().next();
        }
        if (defaultContent != null) {
            notification.setSubject(defaultContent.getSubject());
            notification.setContent(defaultContent.getContent());
        }

        return notification;
    }

    private String renderText(String template, Map<String, Object> params) {
        if (template == null || params == null) {
            return template;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object paramValue = params.get(paramName);

            String replacement;
            if (paramValue == null) {
                replacement = "${" + paramName + "}";
                log.debug("Parameter '{}' not found, keeping placeholder", paramName);
            } else {
                replacement = formatParameterValue(paramValue);
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private String formatParameterValue(Object value) {
        if (value instanceof Instant) {
            return DateTimeFormatter.ISO_INSTANT.format((Instant) value);
        } else if (value instanceof Number) {
            return value.toString();
        } else {
            return String.valueOf(value);
        }
    }

    @Override
    public Mono<Boolean> validateNotificationParameters(Notification notification) {
        if (notification.getTemplateId() == null) {
            return Mono.just(true);
        }

        return templateRepository.findById(notification.getTemplateId())
                .cast(Template.class)
                .map(template -> validateRequiredParams(notification, template))
                .defaultIfEmpty(true);
    }

    private boolean validateRequiredParams(Notification notification, Template template) {
        if (template.getParams() == null || notification.getParams() == null) {
            return true;
        }

        for (Template.TemplateParam param : template.getParams()) {
            if (param.isRequired() && !notification.getParams().containsKey(param.getKey())) {
                log.warn("Required parameter '{}' missing for template {} in notification {}",
                        param.getKey(), template.getId(), notification.getId());
                return false;
            }
        }
        return true;
    }
}
