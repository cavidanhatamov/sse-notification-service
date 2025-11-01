package com.learn.service;

import com.learn.model.Notification;

import reactor.core.publisher.Mono;

/**
 * Service for rendering notification templates with dynamic content.
 */
public interface TemplateRenderingService {

    /**
     * Renders a notification by fetching its template and substituting parameters.
     * Content is rendered in all available languages.
     *
     * @param notification the notification to render
     * @return Mono with the rendered notification
     */
    Mono<Notification> renderNotification(Notification notification);

    /**
     * Validates that all required parameters are present for rendering.
     *
     * @param notification the notification to validate
     * @return Mono with true if valid, false otherwise
     */
    Mono<Boolean> validateNotificationParameters(Notification notification);
}
