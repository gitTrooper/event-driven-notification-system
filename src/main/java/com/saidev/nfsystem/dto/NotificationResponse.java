package com.saidev.nfsystem.dto;

import java.util.UUID;

public class NotificationResponse {

    private UUID notificationId;
    private String status;

    public NotificationResponse(UUID notificationId, String status) {
        this.notificationId = notificationId;
        this.status = status;
    }

    // getters
    public UUID getNotificationId() {
        return notificationId;
    }

    public String getStatus() {
        return status;
    }
    
}
