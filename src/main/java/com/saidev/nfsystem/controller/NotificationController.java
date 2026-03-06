package com.saidev.nfsystem.controller;

import com.saidev.nfsystem.dto.NotificationRequest;
import com.saidev.nfsystem.dto.NotificationResponse;
import com.saidev.nfsystem.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request) {

        NotificationResponse response =
                notificationService.createNotification(request);

        return ResponseEntity.ok(response);
    }
}
