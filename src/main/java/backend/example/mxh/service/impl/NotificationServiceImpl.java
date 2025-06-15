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
        NotificationType type = NotificationType.valueOf(notificationDTO.getType()); // Enum
        String content = type.buildContent(sender.getUsername());
        notification.setType(type);
        notification.setContent(content);
        notificationRepository.save(notification);
        log.info("Created notification: {}", notification);
        NotificationResponse message = notificationMapper.toResponse(notification);
//        socket gui thong bao
        webSocketService.sendNotification(message);
    }

    @Override
    public PageResponse<List<NotificationResponse>> getNotificationsByUserId(int pageNo, int pageSize, Long userId) {
        int page = 0;
        if(pageNo > 0){
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications = notificationRepository.findByReceiver_Id(userId, pageable);
        return PageResponse.<List<NotificationResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(notifications.getTotalElements())
                .totalPages(notifications.getTotalPages())
                .items(notifications.stream().map(notificationMapper::toResponse).toList())
                .build();
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
        notification.setRead(true);
        notificationRepository.save(notification);
        log.info("Marked notification as read: {}", notificationId);
    }

    @Override
    public void markAllNotificationsAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByReceiver_IdAndRead(userId, false);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked all notifications as read for user: {}", userId);
    }

    @Override
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByReceiverIdAndReadIsFalse(userId, false);
    }

}

