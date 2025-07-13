package backend.example.mxh.controller;

import backend.example.mxh.DTO.request.PostsDTO;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.PostsResponse;
import backend.example.mxh.DTO.response.ResponseData;
import backend.example.mxh.service.PostsService;
import backend.example.mxh.until.ResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostsController {

    private final PostsService postsService;

    @PostMapping
    public ResponseData<Long> createPost(@RequestBody @Valid PostsDTO dto) {
        long id = postsService.createPost(dto);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Tạo bài viết thành công", id);
    }

    @PreAuthorize("hasRole('ADMIN') or @postSecurity.isOwner(#id, authentication)")
    @PutMapping("/{id}")
    public ResponseData<Void> updatePost(@PathVariable long id, @RequestBody @Valid PostsDTO dto) throws IOException {
        postsService.updatePost(id, dto);
        return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Cập nhật bài viết thành công");
    }

    @PreAuthorize("hasRole('ADMIN') or @postSecurity.isOwner(#id, authentication)")
    @DeleteMapping("/{id}")
    public ResponseData<Void> deletePost(@PathVariable long id) throws IOException {
        postsService.deletePost(id);
        return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "Xóa bài viết thành công");
    }

    @GetMapping("/{id}")
    public ResponseData<PostsResponse> getPostById(@PathVariable long id) {
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy bài viết thành công", postsService.getPostById(id));
    }


    @GetMapping("/user/{userId}")
    public ResponseData<PageResponse<List<PostsResponse>>> getPostsByUser(@PathVariable long userId,
                                                                                          @RequestParam(defaultValue = "1", required = false) int pageNo,
                                                                                          @RequestParam(defaultValue = "10", required = false) int pageSize,
                                                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Lấy bài viết theo user thành công", postsService.getPostsByUserId(pageNo, pageSize, userId, startDate,endDate));
    }

    @GetMapping
    public ResponseEntity<ResponseData<PageResponse<List<PostsResponse>>>> getAllPosts(@RequestParam(defaultValue = "1", required = false) int pageNo,
                                                                                       @RequestParam(defaultValue = "10", required = false) int pageSize) {
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Lấy danh sách bài viết thành công", postsService.getAllPosts(pageNo, pageSize)));
    }


}
