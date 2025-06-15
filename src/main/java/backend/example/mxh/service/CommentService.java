package backend.example.mxh.service;

import backend.example.mxh.DTO.request.CommentDTO;
import backend.example.mxh.DTO.response.CommentResponse;

import java.util.List;

public interface CommentService {
    Long create(CommentDTO dto);
    void delete(Long id);
    List<CommentResponse> getCommentsByPostId(Long postId);
    void update(Long id, CommentDTO dto);

}
