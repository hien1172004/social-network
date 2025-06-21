package backend.example.mxh.controller;

import backend.example.mxh.DTO.request.*;
import backend.example.mxh.DTO.response.ConversationResponse;
import backend.example.mxh.DTO.response.MemberResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.ResponseData;
import backend.example.mxh.service.ConversationService;
import backend.example.mxh.until.ResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ResponseData<ConversationResponse>> createConversation(@RequestBody @Valid ConversationDTO dto) {
        ConversationResponse response = conversationService.createConversation(dto);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Tạo cuộc trò chuyện thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<ConversationResponse>> getConversationById(
            @PathVariable("id") Long conversationId,
            @RequestParam Long userId) {
        ConversationResponse response = conversationService.getConversationById(conversationId, userId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Lấy thông tin thành công", response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseData<List<ConversationResponse>>> getConversationsByUser(@PathVariable Long userId) {
        List<ConversationResponse> response = conversationService.getConversationsByUserId(userId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Danh sách cuộc trò chuyện", response));
    }

    @PostMapping("/members")
    public ResponseEntity<ResponseData<Void>> addMember(@RequestBody @Valid AddMemberDTO dto) {
        conversationService.addMemberToConversation(dto);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Thêm thành viên thành công"));
    }

    @DeleteMapping("/members")
    public ResponseEntity<ResponseData<Void>> removeMember(@RequestBody @Valid RemoveMemberDTO dto) {
        conversationService.removeMemberFromConversation(dto);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Xóa thành viên thành công"));
    }

    @PutMapping("/update-name")
    public ResponseEntity<ResponseData<Void>> updateName(@RequestBody @Valid UpdateNameConversation dto) throws AccessDeniedException {
        conversationService.updateConversation(dto);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Cập nhật tên nhóm thành công"));
    }

    @PutMapping("/update-role")
    public ResponseEntity<ResponseData<Void>> updateRole(@RequestBody @Valid UpdateMemberRole dto) throws AccessDeniedException {
        conversationService.updateMemberRole(dto);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Cập nhật quyền thành công"));
    }

    @DeleteMapping("/leave")
    public ResponseEntity<ResponseData<Void>> leave(@RequestParam Long conversationId, @RequestParam Long userId) throws BadRequestException {
        conversationService.leaveConversation(conversationId, userId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Rời khỏi cuộc trò chuyện thành công"));
    }

    @DeleteMapping
    public ResponseEntity<ResponseData<Void>> delete(@RequestParam Long conversationId, @RequestParam Long userId) throws BadRequestException {
        conversationService.deleteConversation(conversationId, userId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Xóa cuộc trò chuyện thành công"));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ResponseData<PageResponse<List<MemberResponse>>>> getMembers(
            @PathVariable("id") Long conversationId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResponse<List<MemberResponse>> response = conversationService.getConversationMembers(conversationId, pageNo, pageSize);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Danh sách thành viên", response));
    }

    @GetMapping("/{id}/members/search")
    public ResponseEntity<ResponseData<PageResponse<List<MemberResponse>>>> searchMembers(
            @PathVariable("id") Long conversationId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam String keyword) {
        PageResponse<List<MemberResponse>> response = conversationService.findMemBerInConversation(conversationId, pageNo, pageSize, keyword);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Tìm kiếm thành viên", response));
    }
}
