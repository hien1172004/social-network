package backend.example.mxh.service;

import backend.example.mxh.DTO.request.*;
import backend.example.mxh.DTO.response.ConversationResponse;
import backend.example.mxh.DTO.response.MemberResponse;
import backend.example.mxh.DTO.response.PageResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;

import java.nio.file.AccessDeniedException;
import java.util.List;

// ConversationService.java
public interface ConversationService {
    // Tạo cuộc trò chuyện mới
    Long createConversation(ConversationDTO conversationDTO);
    
    // Lấy thông tin cuộc trò chuyện
    ConversationResponse getConversationById(Long id, Long userId);
    
    // Lấy danh sách cuộc trò chuyện của user
    PageResponse<List<ConversationResponse>> getConversationsByUserId(int pageNo, int pageSize, Long userId);
    
    // Thêm thành viên vào cuộc trò chuyện
    void addMemberToConversation(AddMemberDTO addMemberDTO);
    
    // Xóa thành viên khỏi cuộc trò chuyện
    void removeMemberFromConversation(RemoveMemberDTO removeMemberDTO);
    
    // Cập nhật thông tin cuộc trò chuyện
    void updateConversation(UpdateNameConversation updateNameConversation) throws AccessDeniedException;

    // Cập nhật vai trò thành viên
    void updateMemberRole(UpdateMemberRole updateMemberRole) throws AccessDeniedException;
    
    // Rời khỏi cuộc trò chuyện
    void leaveConversation(Long conversationId, Long userId) throws BadRequestException;
    
    // Xóa cuộc trò chuyện
    void deleteConversationForUser(Long conversationId, Long userId) throws BadRequestException;
    
    // Lấy danh sách thành viên
    PageResponse<List<MemberResponse>> getConversationMembers(Long conversationId, int pageNo, int pageSize);

    PageResponse<List<MemberResponse>> findMemBerInConversation(Long conversationId, int pageNo, int pageSize, String keyword);

}