package backend.example.mxh.controller;

import backend.example.mxh.DTO.request.LikeDTO;
import backend.example.mxh.DTO.response.LikeUserResponse;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    // Toggle like (like nếu chưa có, unlike nếu đã like)
    @PostMapping("/toggle")
    public ResponseEntity<Void> toggleLike(@RequestBody @Valid LikeDTO likeDTO) {
        likeService.toggleLike(likeDTO);
        return ResponseEntity.ok().build();
    }

    // Đếm số lượt like cho một bài post
    @GetMapping("/count/{postId}")
    public ResponseEntity<Long> countLikes(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.countLikes(postId));
    }

    // Lấy danh sách người dùng đã like một bài post (phân trang)
    @GetMapping("/post/{postId}")
    public ResponseEntity<PageResponse<List<LikeUserResponse>>> getUsersWhoLikedPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(likeService.getUserLikePosts(pageNo, pageSize, postId));
    }
}
