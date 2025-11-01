package com.learn.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Simplified DTO for notification filtering parameters
 */
@Data
@NoArgsConstructor
public class NotificationFilterDTO {

    // Essential status filters
    private Boolean read; // Filter by read/unread status
    private String channel; // Filter by channel (EMAIL, SMS, PUSH)
    private String priority; // Filter by priority (LOW, NORMAL, HIGH, URGENT)

    // Pagination - custom setters for validation
    @Setter(AccessLevel.NONE)
    private int page = 0;

    @Setter(AccessLevel.NONE)
    private int size = 20;

    // Sorting - custom setter for validation
    private String sortBy = "timestamps.createdAt";

    @Setter(AccessLevel.NONE)
    private String sortDirection = "DESC"; // DESC or ASC

    // Custom setters with validation
    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public void setSize(int size) {
        // Limit page size between 1 and 100
        this.size = Math.max(1, Math.min(100, size));
    }

    public void setSortDirection(String sortDirection) {
        if ("ASC".equalsIgnoreCase(sortDirection) || "DESC".equalsIgnoreCase(sortDirection)) {
            this.sortDirection = sortDirection.toUpperCase();
        }
    }

    // Custom toString for better logging
    @Override
    public String toString() {
        return "NotificationFilterDTO{" +
                "read=" + read +
                ", channel='" + channel + '\'' +
                ", priority='" + priority + '\'' +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}
