package backend.example.mxh.controller;

import backend.example.mxh.DTO.request.ResetPasswordDTO;
import backend.example.mxh.DTO.request.SignInRequest;
import backend.example.mxh.DTO.request.SignUpDTO;
import backend.example.mxh.DTO.response.ResponseData;
import backend.example.mxh.DTO.response.TokenResponse;
import backend.example.mxh.DTO.response.UserResponse;
import backend.example.mxh.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
//    checked
    @PostMapping("/login-bookstore")
    public ResponseEntity<TokenResponse> accessToken(@RequestBody SignInRequest request) {
        return new ResponseEntity<>(authenticationService.accessToken(request), OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(HttpServletRequest request) {
        return new ResponseEntity<>(authenticationService.refreshToken(request), OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> removeToken(HttpServletRequest request) {
        return new ResponseEntity<>(authenticationService.removeToken(request), OK);
    }

    @PostMapping("/forgot-password")
//    checked
    public ResponseEntity<String> forgotPassword(@RequestBody String email) {
        return new ResponseEntity<>(authenticationService.forgotPassword(email), OK);
    }

    @GetMapping("/reset-password")
//    checked
    public ResponseEntity<String> resetPassword(@RequestParam String token) {
        return new ResponseEntity<>(authenticationService.resetPassword(token), OK);
    }

    @PostMapping("/change-password")
//    checked
    public ResponseEntity<String> changePassword(@RequestBody @Valid ResetPasswordDTO request) {
        return new ResponseEntity<>(authenticationService.changePassword(request), OK);
    }
    @PostMapping("/register")
//    checked
    public ResponseEntity<String> signUp(@RequestBody @Valid SignUpDTO request) {
        log.info("Signing up new user with email: {}", request.getEmail());
        String message = authenticationService.signUp(request);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    // Xác nhận email
    @GetMapping("/verify-email")
//    checked
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        log.info("Verifying email with token: {}", token);
        String message = authenticationService.verifyEmail(token);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @GetMapping("/me")
//    checked
    public ResponseData<UserResponse> getCurrentUser() {
        log.info("Fetching current user info.");
        UserResponse userResponse = authenticationService.getCurrentUser();
        return new ResponseData<>(OK.value(), "success", userResponse);
    }
}