package com.smartcampus.erp.service;

import com.smartcampus.erp.dto.NotificationResponse;
import java.util.List;

public interface NotificationService {
    void createNotification(Long userId, String title, String message);
    List<NotificationResponse> getMyNotifications(Long userId);
    List<NotificationResponse> getMyUnreadNotifications(Long userId);
    void markAsRead(Long userId, Long notificationId);
    void markAllAsRead(Long userId);
}
