package backend.example.mxh.service;

import backend.example.mxh.DTO.request.*;
import backend.example.mxh.DTO.response.ConversationResponse;
import backend.example.mxh.DTO.response.MemberResponse;
import org.apache.coyote.BadRequestException;

import java.nio.file.AccessDeniedException;
import java.util.List;

// ConversationService.java
public interface ConversationService {
    // Tạo cuộc trò chuyện mới
    ConversationResponse createConversation(ConversationDTO conversationDTO);
    
    // Lấy thông tin cuộc trò chuyện
    ConversationResponse getConversationById(Long id);
    
    // Lấy danh sách cuộc trò chuyện của user
    List<ConversationResponse> getConversationsByUserId(Long userId);
    
    // Thêm thành viên vào cuộc trò chuyện
    void addMemberToConversation(AddMemberDTO addMemberDTO);
    
    // Xóa thành viên khỏi cuộc trò chuyện
    void removeMemberFromConversation(RemoveMemberDTO removeMemberDTO);
    
    // Cập nhật thông tin cuộc trò chuyện
    void updateConversation(UpdateNameConversation updateNameConversation) throws AccessDeniedException;
    
    // Cập nhật avatar cuộc trò chuyện
    void updateConversationAvatar(Long conversationId, String avatarUrl);
    
    // Cập nhật vai trò thành viên
    void updateMemberRole(UpdateMemberRole updateMemberRole) throws AccessDeniedException;
    
    // Rời khỏi cuộc trò chuyện
    void leaveConversation(Long conversationId, Long userId) throws BadRequestException;
    
    // Xóa cuộc trò chuyện
    void deleteConversation(Long conversationId, Long userId) throws BadRequestException;
    
    // Lấy danh sách thành viên
    List<MemberResponse> getConversationMembers(Long conversationId);
    
    // Kiểm tra quyền admin
    boolean isAdmin(Long conversationId, Long userId);
}