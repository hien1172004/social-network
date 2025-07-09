package backend.example.mxh.service;

import backend.example.mxh.DTO.request.ResetPasswordDTO;
import backend.example.mxh.DTO.request.SignInRequest;
import backend.example.mxh.DTO.request.SignUpDTO;
import backend.example.mxh.DTO.response.TokenResponse;
import backend.example.mxh.DTO.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;


public interface AuthenticationService {
    TokenResponse accessToken(SignInRequest signInRequest);

    TokenResponse refreshToken(HttpServletRequest request);

    String removeToken(HttpServletRequest request);

    String forgotPassword(String email);

    String resetPassword(String secretKey);

    String changePassword(ResetPasswordDTO request);

    String signUp(SignUpDTO request);

    String verifyEmail(String token);

    UserResponse getCurrentUser();

}
