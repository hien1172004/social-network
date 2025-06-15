package backend.example.mxh.service;

import backend.example.mxh.DTO.request.LikeDTO;
import backend.example.mxh.DTO.response.LikeUserResponse;
import backend.example.mxh.DTO.response.PageResponse;

import java.util.List;

public interface LikeService {
    void toggleLike(LikeDTO likeDTO);
    long countLikes(Long postId);
    PageResponse<List<LikeUserResponse>> getUserLikePosts(int pageNo, int pageSize, Long postId);
}
