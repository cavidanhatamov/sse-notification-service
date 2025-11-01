package com.learn.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "templates")
public class Template {

    @Id
    private String id;

    private String name;
    private String channel;
    private boolean active;
    private List<TemplateParam> params;
    private Map<String, Translation> translations;
    private TemplateMeta meta;

    // Constructors
    public Template() {
    }

    public Template(String id, String name, String channel, boolean active,
            List<TemplateParam> params, Map<String, Translation> translations, TemplateMeta meta) {
        this.id = id;
        this.name = name;
        this.channel = channel;
        this.active = active;
        this.params = params;
        this.translations = translations;
        this.meta = meta;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<TemplateParam> getParams() {
        return params;
    }

    public void setParams(List<TemplateParam> params) {
        this.params = params;
    }

    public Map<String, Translation> getTranslations() {
        return translations;
    }

    public void setTranslations(Map<String, Translation> translations) {
        this.translations = translations;
    }

    public TemplateMeta getMeta() {
        return meta;
    }

    public void setMeta(TemplateMeta meta) {
        this.meta = meta;
    }

    // Inner classes
    public static class TemplateParam {
        private String key;
        private String type;
        private boolean required;
        private String description;

        public TemplateParam() {
        }

        public TemplateParam(String key, String type, boolean required, String description) {
            this.key = key;
            this.type = type;
            this.required = required;
            this.description = description;
        }

        // Getters and Setters
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class Translation {
        private String subject;
        private String content;

        public Translation() {
        }

        public Translation(String subject, String content) {
            this.subject = subject;
            this.content = content;
        }

        // Getters and Setters
        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class TemplateMeta {
        private String createdBy;
        private Instant createdAt;
        private Instant updatedAt;

        public TemplateMeta() {
        }

        public TemplateMeta(String createdBy, Instant createdAt, Instant updatedAt) {
            this.createdBy = createdBy;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        // Getters and Setters
        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        public Instant getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
