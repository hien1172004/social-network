package backend.example.mxh.controller;

import backend.example.mxh.DTO.request.CommentDTO;
import backend.example.mxh.DTO.response.CommentResponse;
import backend.example.mxh.DTO.response.ResponseData;
import backend.example.mxh.service.CommentService;
import backend.example.mxh.until.ResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ResponseData<Long>> createComment(@RequestBody @Valid CommentDTO dto) {
        Long commentId = commentService.create(dto);
        return ResponseEntity.status(201)
                .body(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Tạo bình luận thành công", commentId));
    }

    @PreAuthorize("hasRole('ADMIN') or @commentSecurity.isOwner(#id, authentication)")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> updateComment(@PathVariable Long id,
                                                            @RequestBody @Valid CommentDTO dto) {
        commentService.update(id, dto);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Cập nhật bình luận thành công"));
    }

    @PreAuthorize("hasRole('ADMIN') or @commentSecurity.isOwner(#id, authentication)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteComment(@PathVariable Long id) {
        commentService.delete(id);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Xóa bình luận thành công"));
    }


    @GetMapping("/post/{postId}")
    public ResponseEntity<ResponseData<List<CommentResponse>>> getCommentsByPost(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Lấy bình luận thành công", comments));
    }
}
