package com.learn.repository;

import com.learn.model.Template;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TemplateRepository extends ReactiveMongoRepository<Template, String> {

    /**
     * Find all active templates
     */
    Flux<Template> findByActiveTrue();

    /**
     * Find templates by name
     */
    Flux<Template> findByName(String name);

    /**
     * Find active templates by name
     */
    Flux<Template> findByNameAndActiveTrue(String name);
}


