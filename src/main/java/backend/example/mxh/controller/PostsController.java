package backend.example.mxh.controller;

import backend.example.mxh.DTO.request.PostsDTO;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.PostsResponse;
import backend.example.mxh.DTO.response.ResponseData;
import backend.example.mxh.service.PostsService;
import backend.example.mxh.until.ResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostsController {

    private final PostsService postsService;

    @PostMapping
    public ResponseEntity<ResponseData<Long>> createPost(@RequestBody @Valid PostsDTO dto) {
        long id = postsService.createPost(dto);
        return ResponseEntity.status(201).body(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Tạo bài viết thành công", id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> updatePost(@PathVariable long id, @RequestBody @Valid PostsDTO dto) throws IOException {
        postsService.updatePost(id, dto);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Cập nhật bài viết thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deletePost(@PathVariable long id) throws IOException {
        postsService.deletePost(id);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Xóa bài viết thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<PostsResponse>> getPostById(@PathVariable long id) {
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Lấy bài viết thành công", postsService.getPostById(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseData<PageResponse<List<PostsResponse>>>> getPostsByUser(@PathVariable long userId,
                                                                                          @RequestParam(defaultValue = "1", required = false) int pageNo,
                                                                                          @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Lấy bài viết theo user thành công", postsService.getPostsByUserId(pageNo, pageSize, userId)));
    }

    @GetMapping
    public ResponseEntity<ResponseData<PageResponse<List<PostsResponse>>>> getAllPosts(@RequestParam(defaultValue = "1", required = false) int pageNo,
                                                                                       @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Lấy danh sách bài viết thành công", postsService.getAllPosts(pageNo, pageSize)));
    }
}
