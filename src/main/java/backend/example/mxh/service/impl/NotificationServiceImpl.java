package backend.example.mxh.service.impl;

import backend.example.mxh.DTO.request.NotificationDTO;
import backend.example.mxh.DTO.response.NotificationResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.entity.Notification;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.InvalidDataException;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.NotificationMapper;
import backend.example.mxh.repository.NotificationRepository;
import backend.example.mxh.repository.UserRepository;
import backend.example.mxh.service.NotificationService;
import backend.example.mxh.service.UserService;
import backend.example.mxh.service.WebSocketService;
import backend.example.mxh.until.NotificationType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;
    private final BaseRedisServiceImpl<String, String, PageResponse<List<NotificationResponse>>> baseRedisService;
    private static final String NOTIFICATION_KEY_PREFIX = "notifications:user:";

    @Override
    public void createNotification(NotificationDTO notificationDTO) {
        User sender = userRepository.findById(notificationDTO.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(notificationDTO.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));
        if(Objects.equals(notificationDTO.getSenderId(), notificationDTO.getReceiverId())) {
            throw new InvalidDataException("sender and receiver are the same");
        }
        Notification notification = notificationMapper.toNotification(notificationDTO);
        NotificationType type = notificationDTO.getType(); // Enum
        String content = type.buildContent(sender.getUsername());
        notification.setType(type);
        notification.setContent(content);
        notificationRepository.save(notification);
        log.info("Created notification: {}", notification);
        NotificationResponse message = notificationMapper.toResponse(notification);
        webSocketService.sendNotification(message);
        baseRedisService.deleteByPrefix(NOTIFICATION_KEY_PREFIX + notification.getReceiver().getId());
    }

    @Override
    public PageResponse<List<NotificationResponse>> getNotificationsByUserId(int pageNo, int pageSize, Long userId) {
        String redisKey = NOTIFICATION_KEY_PREFIX + userId + ":page:" + pageNo + ":size:" + pageSize;
        PageResponse<List<NotificationResponse>> cached = baseRedisService.get(redisKey);
        if (cached != null) {
            log.info("Notification cache HIT for {}", redisKey);
            return cached;
        }
        int page = 0;
        if(pageNo > 0){
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications = notificationRepository.findByReceiver_Id(userId, pageable);
        PageResponse<List<NotificationResponse>> response = PageResponse.<List<NotificationResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(notifications.getTotalElements())
                .totalPages(notifications.getTotalPages())
                .items(notifications.stream().map(notificationMapper::toResponse).toList())
                .build();
        baseRedisService.set(redisKey, response);
        baseRedisService.setTimeToLive(redisKey, 60);
        return response;
    }

    @Override
    public PageResponse<List<NotificationResponse>> getUnreadNotificationsByUserId(int pageNo, int pageSize, Long userId) {
        int page = 0;
        if(pageNo > 0){
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications = notificationRepository.findByReceiver_IdAndIsRead(userId, pageable);
        return PageResponse.<List<NotificationResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(notifications.getTotalElements())
                .totalPages(notifications.getTotalPages())
                .items(notifications.stream().map(notificationMapper::toResponse).toList())
                .build();
    }

    @Override
    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if(!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
        log.info("Marked notification as read: {}", notificationId);
        baseRedisService.deleteByPrefix(NOTIFICATION_KEY_PREFIX + notification.getReceiver().getId());
    }

    @Override
    public void markAllNotificationsAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByReceiver_IdAndRead(userId);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked all notifications as read for user: {}", userId);
        baseRedisService.deleteByPrefix(NOTIFICATION_KEY_PREFIX + userId);
    }

    @Override
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByReceiverIdAndReadIsFalse(userId);
    }

}

