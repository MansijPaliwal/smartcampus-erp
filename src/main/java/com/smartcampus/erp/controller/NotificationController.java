package com.smartcampus.erp.controller;

import com.smartcampus.erp.dto.NotificationResponse;
import com.smartcampus.erp.security.UserPrincipal;
import com.smartcampus.erp.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "System notifications and alert logs for users")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Retrieve list of all notifications logged for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications history")
    public ResponseEntity<List<NotificationResponse>> getNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(notificationService.getMyNotifications(userPrincipal.getId()));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Retrieve unread notifications logs only.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved unread notifications")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(notificationService.getMyUnreadNotifications(userPrincipal.getId()));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark single notification as read", description = "Change a notification log's status to read by its ID.")
    @ApiResponse(responseCode = "200", description = "Successfully marked notification as read")
    @ApiResponse(responseCode = "404", description = "Notification not found or does not belong to logged-in user")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        notificationService.markAsRead(userPrincipal.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Bulk update all unread notifications of the logged-in user to read.")
    @ApiResponse(responseCode = "200", description = "Successfully updated all notifications to read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        notificationService.markAllAsRead(userPrincipal.getId());
        return ResponseEntity.ok().build();
    }
}
