package backend.example.mxh.controller;

import backend.example.mxh.DTO.request.AddUserDTO;
import backend.example.mxh.DTO.request.ImageDTO;
import backend.example.mxh.DTO.request.UpdateUserDTO;
import backend.example.mxh.DTO.response.PageResponse;
import backend.example.mxh.DTO.response.ResponseData;
import backend.example.mxh.DTO.response.UserResponse;
import backend.example.mxh.service.UserService;
import backend.example.mxh.until.ResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Thêm người dùng mới
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ResponseData<Long>> createUser(@RequestBody @Valid AddUserDTO dto) {
        long userId = userService.addUser(dto);
        return ResponseEntity.status(201)
                .body(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Tạo người dùng thành công", userId));
    }

    /**
     * Cập nhật thông tin người dùng
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ResponseData<Void>> updateUser(@PathVariable("id") long id,
                                           @RequestBody @Valid UpdateUserDTO dto) {
        userService.updateUser(id, dto);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Cập nhật người dùng thành công"));
    }

    /**
     * Cập nhật ảnh đại diện (avatar)
     */
    @PutMapping("/{id}/avatar")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ResponseData<Void>> updateAvatar(@PathVariable("id") long id,
                                             @RequestBody @Valid ImageDTO dto) throws IOException {
        userService.updateAvatar(id, dto);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Cập nhật avatar thành công"));
    }

    /**
     * Lấy thông tin chi tiết người dùng
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<UserResponse>> getUserById(@PathVariable("id") long id) {
        UserResponse response = userService.getDetailUser(id);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Lấy thông tin người dùng thành công", response));
    }

    /**
     * Tìm kiếm người dùng (có phân trang + từ khóa)
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseData<PageResponse<List<UserResponse>>>> searchUsers(
            @RequestParam(defaultValue = "1", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String keyword
    ) {
        PageResponse<List<UserResponse>> result = userService.searchUser(pageNo, pageSize, keyword);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Tìm kiếm người dùng thành công", result));
    }

    /**
     * Xoá mềm người dùng (inactive)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ResponseData<Void>> deleteUser(@PathVariable("id") long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Xoá người dùng thành công"));
    }


    @GetMapping("/online")
    public ResponseEntity<ResponseData<PageResponse<List<UserResponse>>>> getOnlineUsers(
            @RequestParam(defaultValue = "1", required = false) int pageNo,
            @RequestParam(defaultValue = "10", required = false) int pageSize
    ) {
        PageResponse<List<UserResponse>> result = userService.getUsersOnline(pageNo, pageSize);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "Lấy ngươi dung online thanh cong", result));
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseData<PageResponse<List<UserResponse>>>> getAllUsers( @RequestParam(defaultValue = "1", required = false) int pageNo,
                                                                                       @RequestParam(defaultValue = "10", required = false) int pageSize,
                                                                                       @RequestParam(required = false) String keyword,
                                                                                       @RequestParam(required = false) String... sortBy){
        PageResponse<List<UserResponse>> result = userService.getAllUsers(pageNo, pageSize, keyword, sortBy);
        return ResponseEntity.ok(new ResponseData<>(ResponseCode.SUCCESS.getCode(), "thanh cong", result));
    }
}
