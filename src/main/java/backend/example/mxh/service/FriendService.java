package backend.example.mxh.service;

import backend.example.mxh.DTO.request.FriendDTO;
import backend.example.mxh.DTO.response.FriendResponse;
import backend.example.mxh.DTO.response.MutualFriendResponse;
import backend.example.mxh.DTO.response.PageResponse;

import java.util.List;

public interface FriendService {
    Long sendFriendRequest(FriendDTO dto);

    void acceptFriendRequest(Long friendRequestId);

    void declineFriendRequest(Long friendRequestId);

    void unAcceptFriendRequest(Long userId1, Long userId2);

    PageResponse< List<FriendResponse>> getReceivedFriendRequests(int pageNo, int pageSize, Long userId);

    PageResponse< List<FriendResponse>> getSentFriendRequests(int pageNo, int pageSize, Long userId);

    PageResponse< List<FriendResponse>> getFriends(int pageNo, int pageSize, Long userId);

    PageResponse< List<MutualFriendResponse>> getMutualFriends(Long userId1, Long userId2, int pageNo, int pageSize);
}
