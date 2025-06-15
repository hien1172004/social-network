package backend.example.mxh.service;

import backend.example.mxh.DTO.request.NotificationDTO;
import backend.example.mxh.DTO.response.NotificationResponse;
import backend.example.mxh.DTO.response.PageResponse;

import java.util.List;

public interface NotificationService {
    void createNotification(NotificationDTO notificationDTO);
    PageResponse<List<NotificationResponse>> getNotificationsByUserId(int pageNo, int pageSize, Long userId);
    PageResponse<List<NotificationResponse>> getUnreadNotificationsByUserId(int pageNo, int pageSize, Long userId);
    void markNotificationAsRead(Long notificationId);
    void markAllNotificationsAsRead(Long userId);
    long countUnreadNotifications(Long userId);
}