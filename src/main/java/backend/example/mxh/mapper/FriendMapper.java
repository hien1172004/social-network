package backend.example.mxh.mapper;

import backend.example.mxh.DTO.request.FriendDTO;
import backend.example.mxh.DTO.response.FriendResponse;
import backend.example.mxh.entity.Friend;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FriendMapper {
    @Mapping(target = "sender.id", source = "senderId")
    @Mapping(target = "receiver.id", source = "receiverId")
    Friend toFriend(FriendDTO dto);

    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderUserName", source = "sender.username")
    @Mapping(target = "senderAvatar", source = "sender.avatarUrl")
    @Mapping(target = "receiverId", source = "receiver.id")
    @Mapping(target = "receiverUserName", source = "receiver.username")
    @Mapping(target = "receiverAvatar", source = "receiver.avatarUrl")
    @Mapping(target = "status", source = "status")
    FriendResponse toFriendResponse(Friend friend);
}
