package backend.example.mxh.security;

import backend.example.mxh.entity.Notification;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("notificationSecurity")
@RequiredArgsConstructor
public class NotificationSecurity {
    private final NotificationRepository notificationRepository;

    public boolean isOwner(final Long notificationId, Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(()
                -> new ResourceNotFoundException("Notification not found"));
        return userId.equals(notification.getReceiver().getId());
    }
}
