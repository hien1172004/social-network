package backend.example.mxh.service;

import backend.example.mxh.DTO.request.PostsDTO;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.PostsResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface PostsService {
    long createPost(PostsDTO postsDTO);

    void updatePost(long id, PostsDTO postsDTO) throws IOException;

    void deletePost(long id) throws IOException;

    PostsResponse getPostById(long id);

    PageResponse<List<PostsResponse>> getAllPosts(int pageNo, int pageSize);

    PageResponse<List<PostsResponse>> getPostsByUserId(int pageNo, int pageSize, long userId, LocalDateTime startDate, LocalDateTime endDate);


}
