package backend.example.mxh.service;

import backend.example.mxh.DTO.request.ResetPasswordDTO;
import backend.example.mxh.DTO.request.SignInRequest;
import backend.example.mxh.DTO.request.SignUpDTO;
import backend.example.mxh.DTO.response.TokenResponse;
import backend.example.mxh.DTO.response.UserResponse;
import backend.example.mxh.entity.RedisToken;
import backend.example.mxh.entity.User;
import backend.example.mxh.exception.InvalidDataException;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.mapper.UserMapper;
import backend.example.mxh.repository.UserRepository;
import backend.example.mxh.until.AccountStatus;
import backend.example.mxh.until.TokenType;
import backend.example.mxh.until.UserRole;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static backend.example.mxh.until.TokenType.RESET_TOKEN;
import static backend.example.mxh.until.TokenType.VERIFICATION_TOKEN;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTokenService redisTokenService;
    private final UserService userService;
    private final EmailService emailService;
    private final UserMapper userMapper;

    @Transactional
    public TokenResponse accessToken(SignInRequest signInRequest){
       log.info("-------authenticate----");
       var user = userRepository.findByEmail(signInRequest.getEmail()).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        if (!user.isEnabled()) {
            throw new InvalidDataException("user not active");
        }
//
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signInRequest.getEmail(),
                            signInRequest.getPassword(),
                            Collections.singleton(new SimpleGrantedAuthority(user.getRole().toString()))
                    )
            );
        } catch (BadCredentialsException e) {
            throw new InvalidDataException("Email hoặc mật khẩu không đúng");
        }
        log.info("Login success for user: {}", signInRequest.getEmail());
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        redisTokenService.save(RedisToken.builder()
                .id(user.getEmail())
                .refreshToken(refreshToken)
                .build());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }
    /**
     * Refresh token
     *
     * @param request
     * @return
     */
    public TokenResponse refreshToken(HttpServletRequest request) {
        log.info("---------- refreshToken ----------");

        final String refreshToken = request.getHeader(HttpHeaders.REFERER);
        log.info("RefreshToken: {}", refreshToken);
        if (StringUtils.isBlank(refreshToken)) {
            throw new InvalidDataException("Token must be not blank");
        }

        final String email =jwtService.extractUsername(refreshToken, TokenType.REFRESH_TOKEN);
        log.info("username:{}", email);
        var user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        log.info("userID: {}", user.getId());

        if(!jwtService.isValid( refreshToken, TokenType.REFRESH_TOKEN, user)){
            throw new InvalidDataException("Not allow access with this token");
        }

        String accessToken = jwtService.generateToken(user);
        redisTokenService.save(RedisToken.builder()
                .id(user.getEmail())
                .refreshToken(refreshToken)
                .build());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }
    /**
     * Logout
     *
     * @param request
     * @return
     */
    @Transactional
    public String removeToken(HttpServletRequest request) {
        log.info("---------- removeToken ----------");

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        token = token.substring(7);
        log.info("Token: {}", token);
        if (StringUtils.isBlank(token)) {
            throw new InvalidDataException("Token không hợp lệ hoặc thiếu");
        }

        final String email = jwtService.extractUsername(token, TokenType.ACCESS_TOKEN);

        redisTokenService.delete(email);

        return "Removed!";
    }
    /**
     * Forgot password
     *
     * @param email
     */
    @Transactional
    public String forgotPassword(String email) {
        log.info(email);
        email = email.trim();

// Loại bỏ dấu ngoặc kép 2 đầu nếu có
        if (email.startsWith("\"") && email.endsWith("\"") && email.length() > 1) {
            email = email.substring(1, email.length() - 1);
        }
        log.info("---------- forgotPassword ----------");
        log.info("email: {}", email);
        // check email exists or not
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // generate reset token
        String resetToken = jwtService.generateResetToken(user);

        // save to db
        redisTokenService.save(RedisToken.builder().id(user.getEmail()).resetToken(resetToken).build());

        // Gửi email chứa liên kết đặt lại mật khẩu
        try {
            emailService.sendResetPasswordEmail(user.getEmail(), resetToken);
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email đặt lại mật khẩu: {}", e.getMessage());
            throw new InvalidDataException("Không thể gửi email đặt lại mật khẩu");
        }
        String confirmLink = String.format("curl --location 'http://localhost:8080/auth/reset-password' \\\n" +
                "--header 'accept: */*' \\\n" +
                "--header 'Content-Type: application/json' \\\n" +
                "--data '%s'", resetToken);
        log.info("--> confirmLink: {}", confirmLink);

        return resetToken;
    }
    /**
     * Reset password
     *
     * @param secretKey
     * @return
     */
    @Transactional
    public String resetPassword(String secretKey) {
        log.info("---------- resetPassword ----------");

        // validate token
        var user = validateToken(secretKey);

        // check token by email
        redisTokenService.getById(user.getEmail());

        return "Reset";
    }
    @Transactional
    public String changePassword(ResetPasswordDTO request) {
        log.info("---------- changePassword ----------");

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataException("Passwords do not match");
        }

        // get user by reset token
        var user = validateToken(request.getSecretKey());

        // update password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return "Changed";
    }

    /**
     * Validate user and reset token
     *
     * @param token
     * @return
     */
    private User validateToken(String token) {
        // validate token
        var email = jwtService.extractUsername(token, RESET_TOKEN);

        // validate user is active or not
        var user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("not found user"));
        if (!user.isEnabled()) {
            throw new InvalidDataException("User not active");
        }

        return user;
    }
    // Đăng ký người dùng mới
    @Transactional
    public String signUp(SignUpDTO request){
        log.info("---------- signUp ----------");

        // Kiểm tra email đã tồn tại
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InvalidDataException("Email đã được sử dụng");
        }

        // Kiểm tra mật khẩu khớp
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataException("Mật khẩu không khớp");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER); // Mặc định là USER
        user.setAccountStatus(AccountStatus.LOCKED);
        String verificationToken = jwtService.generateVerificationToken(user);
        // Lưu verification token vào Token
        redisTokenService.save(RedisToken.builder()
                .id(user.getEmail())
                .verificationToken(verificationToken)
                .build());
       log.info("verify Token: {}",verificationToken);
        userRepository.save(user);
        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email xác nhận: {}", e.getMessage());
            throw new InvalidDataException("Không thể gửi email xác nhận");
        }
        return "Đăng ký thành công! Vui lòng kiểm tra email để xác nhận.";
    }
    /**
     * Xác nhận email
     *
     * @param token Token xác nhận
     * @return Thông báo thành công
     */
    public String verifyEmail(String token) {
        log.info("---------- verifyEmail ----------");

        // Xác thực token
        String email = jwtService.extractUsername(token, VERIFICATION_TOKEN);
        var user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("not found user"));

        // Kiểm tra token khớp
        if (!jwtService.isValid(token, VERIFICATION_TOKEN, user)) {
            throw new InvalidDataException("Token xác nhận không hợp lệ hoặc đã hết hạn");
        }
        // Kiểm tra token trong cơ sở dữ liệu
        RedisToken storedToken = redisTokenService.getById(email);
        if (storedToken == null || !token.equals(storedToken.getVerificationToken())) {
            throw new InvalidDataException("Token xác nhận không khớp");
        }
        // Kích hoạt tài khoản
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        // Xóa verification token khỏi Token
       redisTokenService.delete(email);

        return "Tài khoản đã được kích hoạt thành công!";
    }

    @Transactional
    public UserResponse getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            // Chuyển đổi thành UserDetails
            String email = userDetails.getUsername();

            var user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("not found user"));

            return userMapper.toUserResponse(user);
        } else {
            // Trường hợp nếu principal không phải là UserDetails (chẳng hạn như String)
            throw new BadCredentialsException("Không tìm thấy thông tin người dùng.");
        }
    }
}
