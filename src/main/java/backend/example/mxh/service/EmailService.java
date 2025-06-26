package backend.example.mxh.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;
    /**
     * Gửi email chứa liên kết đặt lại mật khẩu
     *
     * @param toEmail        Địa chỉ email người nhận
     * @param resetToken  Token đặt lại mật khẩu
     * @throws MessagingException Nếu có lỗi khi gửi email
     */
    public void sendResetPasswordEmail(String toEmail, String resetToken) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        // URL cơ sở cho liên kết đặt lại mật khẩu (có thể cấu hình trong application.properties)
        String resetUrl = "http://localhost:3000/dat-lai-mat-khau/?token=" + resetToken;

        // Nội dung email (HTML)
        String htmlContent = "<h3>Đặt lại mật khẩu</h3>" +
                "<p>Vui lòng nhấp vào liên kết dưới đây để đặt lại mật khẩu của bạn:</p>" +
                "<p><a href=\"" + resetUrl + "\">Đặt lại mật khẩu</a></p>" +
                "<p>Liên kết này sẽ hết hạn sau 1 giờ.</p>" +
                "<p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>";

        helper.setTo(toEmail);
        helper.setSubject("Yêu cầu đặt lại mật khẩu");
        helper.setText(htmlContent, true); // true: hỗ trợ HTML
        helper.setFrom(fromEmail); // Thay bằng email của bạn

        mailSender.send(mimeMessage);
        log.info("Đã gửi email đặt lại mật khẩu đến: {}", toEmail);
    }
    /**
     * Gửi email chứa liên kết xác nhận đăng ký
     *
     * @param to                Địa chỉ email người nhận
     * @param verificationToken Token xác nhận email
     * @throws MessagingException Nếu có lỗi khi gửi email
     */
    public void sendVerificationEmail(String to, String verificationToken) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        // Đường dẫn đến trang xác nhận tài khoản
        String verificationUrl = "http://localhost:3000/auth/verify-email?token=" + verificationToken;

        // Nội dung email dạng HTML
        String htmlContent = "<h3>Xác nhận đăng ký tài khoản</h3>" +
                "<p>Cảm ơn bạn đã đăng ký tài khoản với chúng tôi! Vui lòng nhấp vào liên kết dưới đây để kích hoạt tài khoản của bạn:</p>" +
                "<p><a href=\"" + verificationUrl + "\">Kích hoạt tài khoản</a></p>" +
                "<p>Liên kết này sẽ hết hạn sau 24 giờ.</p>" +
                "<p>Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email này.</p>";

        helper.setTo(to);
        helper.setSubject("Xác nhận đăng ký tài khoản");
        helper.setText(htmlContent, true);
        helper.setFrom(fromEmail);

        mailSender.send(mimeMessage);
        log.info("Đã gửi email xác nhận đăng ký đến: {}", to);
    }

}

