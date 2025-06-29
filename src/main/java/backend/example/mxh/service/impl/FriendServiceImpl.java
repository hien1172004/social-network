package backend.example.mxh.service.impl;


import backend.example.mxh.DTO.request.FriendDTO;
import backend.example.mxh.DTO.request.NotificationDTO;
import backend.example.mxh.DTO.response.FriendResponse;
import backend.example.mxh.DTO.response.MutualFriendResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.entity.Friend;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.InvalidDataException;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.FriendMapper;
import backend.example.mxh.mapper.UserMapper;
import backend.example.mxh.repository.FriendRepository;
import backend.example.mxh.repository.UserRepository;
import backend.example.mxh.service.BaseRedisService;
import backend.example.mxh.service.FriendService;
import backend.example.mxh.service.NotificationService;
import backend.example.mxh.until.FriendStatus;
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

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FriendServiceImpl implements FriendService {
    private final FriendRepository friendRepository;
    private final FriendMapper friendMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final BaseRedisService<String, String, PageResponse<List<FriendResponse>>> baseRedisServicePage;
    private final BaseRedisService<String, String, PageResponse<List<MutualFriendResponse>>> baseRedisServiceMutual;

    @Override
    public Long sendFriendRequest(FriendDTO dto) {
        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        if (Objects.equals(receiver.getId(), sender.getId())) {
            throw new InvalidDataException("Receiver and Sender are the same");
        }
        int exists = friendRepository.existsFriendship(sender.getId(), receiver.getId());

        if (exists > 0) {
            throw new IllegalArgumentException("Friend request already exists or already friends.");
        }

        Friend friend = new Friend();
        friend.setSender(sender);
        friend.setReceiver(receiver);
        friend.setStatus(FriendStatus.PENDING);
        friendRepository.save(friend);
        log.info("Friend request sent.");
        // Gửi thông báo sau khi gửi lời mời
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .senderId(dto.getSenderId())
                .receiverId(dto.getReceiverId())
                .type(NotificationType.FRIEND_REQUEST) // Enum
                .referenceId(sender.getId())
                .build();
        notificationService.createNotification(notificationDTO);
        return friend.getId();
    }

    @Override
    public void acceptFriendRequest(Long senderId, Long receiverId) {
        Friend friend = friendRepository.findBySender_IdAndReceiver_Id(senderId, receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        friend.setStatus(FriendStatus.ACCEPTED);
        friendRepository.save(friend);
        log.info("Accepted friend request: {}", friend);

        // Clear cache
        baseRedisServicePage.deleteByPrefix("friends:list:user:" + senderId);
        baseRedisServicePage.deleteByPrefix("friends:list:user:" + receiverId);
        baseRedisServiceMutual.deleteByPrefix("friends:mutual:user:"); // Hoặc pattern delete toàn bộ mutual

        // Gửi thông báo sau khi gửi lời mời
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .senderId(friend.getSender().getId())
                .receiverId(friend.getReceiver().getId())
                .type(NotificationType.ACCEPT_REQUEST) // Enum
                .build();
        notificationService.createNotification(notificationDTO);
    }

    @Override
    public void declineFriendRequest(Long senderId, Long receiverId) {
        Friend friend = friendRepository.findBySender_IdAndReceiver_Id(senderId, receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));
        if (friend.getStatus() == FriendStatus.ACCEPTED) {
            throw new InvalidDataException("Friend request already accepted");
        }
        friend.setStatus(FriendStatus.DECLINED);
        friendRepository.save(friend);
        log.info("decline friend request: {}", friend);
    }

    @Override
    public void unAcceptFriendRequest(Long userId1, Long userId2) {
        if (userId1.equals(userId2)) {
            throw new InvalidDataException("Cannot unfriend yourself");
        }
        friendRepository.unfriend(userId1, userId2, FriendStatus.ACCEPTED);
        log.info("User {} unfriended user {}", userId1, userId2);

        // xoa cache
        baseRedisServicePage.deleteByPrefix("friends:list:user:" + userId1);
        baseRedisServicePage.deleteByPrefix("friends:list:user:" + userId2);
        baseRedisServiceMutual.deleteByPrefix("friends:mutual:user:");
    }


    // thu hoi
    @Override
    public void cancelFriendRequest(Long senderId, Long receiverId) {
        Friend friend = friendRepository.getFriendRequest(senderId, receiverId, FriendStatus.PENDING, FriendStatus.DECLINED).orElseThrow(()
                -> new ResourceNotFoundException("Friend request not found"));
        friendRepository.delete(friend);
        log.info("User {} cancelled friend request {}", senderId, receiverId);
    }

    @Override
    public PageResponse<List<FriendResponse>> getReceivedFriendRequests(int pageNo, int pageSize, Long userId) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Friend> friends = friendRepository.findByReceiver_IdAndStatus(userId, FriendStatus.PENDING, pageable);
        return PageResponse.<List<FriendResponse>>builder()
                .pageSize(pageSize)
                .pageNo(pageNo)
                .items(friends.stream().map(friendMapper::toFriendResponse).toList())
                .totalElements(friends.getTotalElements())
                .totalPages(friends.getTotalPages())
                .build();
    }

    @Override
    public PageResponse<List<FriendResponse>> getSentFriendRequests(int pageNo, int pageSize, Long userId) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Friend> friends = friendRepository.findBySender_IdAndStatus(userId, FriendStatus.PENDING, pageable);
        return PageResponse.<List<FriendResponse>>builder()
                .pageSize(pageSize)
                .pageNo(pageNo)
                .items(friends.stream().map(friendMapper::toFriendResponse).toList())
                .totalElements(friends.getTotalElements())
                .totalPages(friends.getTotalPages())
                .build();
    }

    @Override
    public PageResponse<List<FriendResponse>> getFriends(int pageNo, int pageSize, Long userId) {
        String redisKey = "friends:list:user:" + userId + ":page:" + pageNo + "-" + pageSize;
        PageResponse<List<FriendResponse>> cached = baseRedisServicePage.get(redisKey);
        if (cached != null) return cached;

        int page = (pageNo > 0) ? pageNo - 1 : 0;

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Friend> friends = friendRepository.findAcceptedFriends(userId, FriendStatus.ACCEPTED, pageable);

        PageResponse<List<FriendResponse>> response = PageResponse.<List<FriendResponse>>builder()
                .pageSize(pageSize)
                .pageNo(pageNo)
                .items(friends.stream().map(friendMapper::toFriendResponse).toList())
                .totalElements(friends.getTotalElements())
                .totalPages(friends.getTotalPages())
                .build();

        baseRedisServicePage.set(redisKey, response);
        baseRedisServicePage.setTimeToLive(redisKey, 180);
        return response;
    }

    @Override
    public PageResponse<List<MutualFriendResponse>> getMutualFriends(Long userId1, Long userId2, int pageNo, int pageSize) {
        String redisKeyMutual = "friends:mutual:user:" + userId1 + "-" + userId2 + ":page:" + pageNo + "-" + pageSize;
        PageResponse<List<MutualFriendResponse>> cached = baseRedisServiceMutual.get(redisKeyMutual);
        if(cached != null){
            log.info("User {} mutual friends found", userId1);
            return cached;
        }
        if(userId1.equals(userId2)) {
            throw new InvalidDataException("userId1 equals userId2");
        }
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Lấy danh sách ID bạn bè của từng user
        List<Long> friends1 = friendRepository.findFriendIds(userId1, FriendStatus.ACCEPTED);
        List<Long> friends2 = friendRepository.findFriendIds(userId2, FriendStatus.ACCEPTED);

        // Giao nhau
        friends1.retainAll(friends2);
        if (friends1.isEmpty()) {
            return PageResponse.<List<MutualFriendResponse>>builder()
                    .pageSize(pageSize)
                    .pageNo(pageNo)
                    .items(List.of())
                    .totalElements(0L)
                    .totalPages(0)
                    .build();
        }
        // Lấy danh sách bạn bè chung từ User
        Page<User> mutualFriends = userRepository.findByIdIn(friends1, pageable);
        PageResponse<List<MutualFriendResponse>> response = PageResponse.<List<MutualFriendResponse>>builder()
                .pageSize(pageSize)
                .pageNo(pageNo)
                .items(mutualFriends.stream().map(userMapper::toMutualFriendResponse).toList())
                .totalElements(mutualFriends.getTotalElements())
                .totalPages(mutualFriends.getTotalPages())
                .build();
        baseRedisServiceMutual.set(redisKeyMutual, response);
        baseRedisServiceMutual.setTimeToLive(redisKeyMutual, 180);
        return response;
    }

    @Override
    public PageResponse<List<MutualFriendResponse>> getSuggestFriends(int pageNo, int pageSize, Long userId) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Long> suggestFriend = friendRepository.findFriendSuggestions(userId);
        Page<User> mutualFriends = userRepository.findByIdIn(suggestFriend, pageable);
        return PageResponse.<List<MutualFriendResponse>>builder()
                .pageSize(pageSize)
                .pageNo(pageNo)
                .items(mutualFriends.stream().map(userMapper::toMutualFriendResponse).toList())
                .totalElements(mutualFriends.getTotalElements())
                .totalPages(mutualFriends.getTotalPages())
                .build();
    }

    @Override
    public Long countFriends(Long userId) {
        return friendRepository.countFriendB(userId, FriendStatus.ACCEPTED);
    }
}
