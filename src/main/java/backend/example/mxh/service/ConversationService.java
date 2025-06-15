package backend.example.mxh.service;

import backend.example.mxh.DTO.request.AddMemberDTO;
import backend.example.mxh.DTO.request.ConversationDTO;
import backend.example.mxh.DTO.request.ConversationMemberDTO;
import backend.example.mxh.DTO.request.RemoveMemberDTO;
import backend.example.mxh.DTO.response.ConversationResponse;

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
    void updateConversation(Long conversationId, ConversationDTO conversationDTO);
    
    // Cập nhật avatar cuộc trò chuyện
    void updateConversationAvatar(Long conversationId, String avatarUrl);
    
    // Cập nhật vai trò thành viên
    void updateMemberRole(Long conversationId, Long userId, String role);
    
    // Rời khỏi cuộc trò chuyện
    void leaveConversation(Long conversationId, Long userId);
    
    // Xóa cuộc trò chuyện
    void deleteConversation(Long conversationId);
    
    // Lấy danh sách thành viên
    List<ConversationMemberDTO> getConversationMembers(Long conversationId);
    
    // Kiểm tra quyền admin
    boolean isAdmin(Long conversationId, Long userId);
}