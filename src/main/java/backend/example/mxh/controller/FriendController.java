package backend.example.mxh.controller;

import backend.example.mxh.DTO.request.FriendDTO;
import backend.example.mxh.DTO.response.FriendResponse;
import backend.example.mxh.DTO.response.MutualFriendResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.ResponseData;
import backend.example.mxh.service.FriendService;
import backend.example.mxh.until.ResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    /**
     * Gửi lời mời kết bạn
     */
    @PostMapping("/request")
    @PreAuthorize("#dto.senderId == authentication.principal.id")
    public ResponseEntity<ResponseData<Long>> sendFriendRequest(@RequestBody @Valid FriendDTO dto) {
        Long requestId = friendService.sendFriendRequest(dto);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Gửi lời mời kết bạn thành công", requestId));
    }

    /**
     * Chấp nhận lời mời kết bạn
     */
    @PutMapping("/{receiverId}/accept/{senderId}")
    @PreAuthorize("#senderId == authentication.principal.id")
    public ResponseEntity<ResponseData<Void>> acceptRequest(@PathVariable("senderId") Long senderId,
                                                            @PathVariable("receiverId") Long receiverId) {
        friendService.acceptFriendRequest(senderId, receiverId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Chấp nhận lời mời thành công"));
    }

    /**
     * Từ chối lời mời kết bạn
     */
    @PutMapping("/{receiverId}/decline/{senderId}")
    @PreAuthorize("#senderId == authentication.principal.id")
    public ResponseEntity<ResponseData<Void>> declineRequest(@PathVariable("senderId") Long senderId,
                                                             @PathVariable("receiverId") Long receiverId) {
        friendService.declineFriendRequest(senderId, receiverId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Từ chối lời mời thành công"));
    }

    /**
     * Hủy kết bạn
     */
    @DeleteMapping("/unfriend/{senderId}/{receiverId}")
    @PreAuthorize("#userId1 == authentication.principal.id")
    public ResponseEntity<ResponseData<Void>> unfriend(@PathVariable("senderId") Long userId1, @PathVariable("receiverId") Long userId2) {
        friendService.unAcceptFriendRequest(userId1, userId2);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Đã huỷ kết bạn"));
    }
    /**
     * thu hồi lời kết bạn
     */
    @DeleteMapping("/cancel")
    @PreAuthorize("#senderId == authentication.principal.id")
    public ResponseEntity<ResponseData<Void>> cancelFriendRequest(@RequestParam Long senderId,
                                                 @RequestParam Long receiverId) {
        friendService.cancelFriendRequest(senderId, receiverId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "đã thu hồi lời mời kết bạn"));
    }
    /**
     * Lấy danh sách lời mời kết bạn đã nhận
     */
    @GetMapping("/received")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<ResponseData<PageResponse<List<FriendResponse>>>> getReceivedRequests(
            @RequestParam(defaultValue = "1", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam Long userId) {
        PageResponse<List<FriendResponse>> response = friendService.getReceivedFriendRequests(pageNo, pageSize, userId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Danh sách lời mời nhận", response));
    }

    /**
     * Lấy danh sách lời mời kết bạn đã gửi
     */
    @GetMapping("/sent")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<ResponseData<PageResponse<List<FriendResponse>>>> getSentRequests(
            @RequestParam(defaultValue = "1", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam Long userId) {
        PageResponse<List<FriendResponse>> response = friendService.getSentFriendRequests(pageNo, pageSize, userId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Danh sách lời mời đã gửi", response));
    }

    /**
     * Lấy danh sách bạn bè
     */
    @GetMapping
    public ResponseEntity<ResponseData<PageResponse<List<FriendResponse>>>> getFriends(
            @RequestParam(defaultValue = "1", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam Long userId) {
        PageResponse<List<FriendResponse>> response = friendService.getFriends(pageNo, pageSize, userId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Danh sách bạn bè", response));
    }

    /**
     * Lấy danh sách bạn chung giữa 2 người
     */

    @GetMapping("/mutual")
    @PreAuthorize("#userId1 == authentication.principal.id or #userId2 == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<ResponseData<PageResponse<List<MutualFriendResponse>>>> getMutualFriends(
            @RequestParam Long userId1,
            @RequestParam Long userId2,
            @RequestParam(defaultValue = "1", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize) {
        PageResponse<List<MutualFriendResponse>> response = friendService.getMutualFriends(userId1, userId2, pageNo, pageSize);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Bạn chung", response));
    }

    /**
     * Gợi ý kết bạn
     */
    @GetMapping("/suggest")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<ResponseData<PageResponse<List<MutualFriendResponse>>>> suggestFriends(
            @RequestParam int pageNo,
            @RequestParam int pageSize,
            @RequestParam Long userId) {
        PageResponse<List<MutualFriendResponse>> response = friendService.getSuggestFriends(pageNo, pageSize, userId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Gợi ý kết bạn", response));
    }

    /**
     * Đếm số lượng bạn bè
     */
    @GetMapping("/count")
    public ResponseEntity<ResponseData<Long>> countFriends(@RequestParam Long userId) {
        Long count = friendService.countFriends(userId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Số lượng bạn bè", count));
    }
}
