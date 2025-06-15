package backend.example.mxh.mapper;

import backend.example.mxh.DTO.request.NotificationDTO;
import backend.example.mxh.DTO.response.NotificationResponse;
import backend.example.mxh.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    // Chuyển từ NotificationDTO sang Notification
    @Mapping(target = "sender.id", source = "senderId")
    @Mapping(target = "receiver.id", source = "receiverId")
    Notification toNotification(NotificationDTO dto);

    // Chuyển từ Notification sang NotificationResponse
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderUsername", source = "sender.username")
    @Mapping(target = "receiverId", source = "receiver.id")
    NotificationResponse toResponse(Notification notification);

    // Cập nhật Notification từ NotificationDTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "receiver", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateNotification(@MappingTarget Notification notification, NotificationDTO dto);
}
