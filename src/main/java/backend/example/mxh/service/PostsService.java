package backend.example.mxh.service;

import backend.example.mxh.DTO.request.PostsDTO;
import backend.example.mxh.DTO.response.PostsResponse;

import java.io.IOException;
import java.util.List;

public interface PostsService {
    long createPost(PostsDTO postsDTO);

    void updatePost(long id, PostsDTO postsDTO) throws IOException;

    void deletePost(long id) throws IOException;

    PostsResponse getPostById(long id);

    List<PostsResponse> getAllPosts();

    List<PostsResponse> getPostsByUserId(long userId);
}
