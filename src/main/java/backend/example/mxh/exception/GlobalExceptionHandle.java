package backend.example.mxh.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.security.SignatureException;
import java.util.Date;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandle {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnknownException(Exception e, WebRequest request) {
        log.error("❌ Unknown internal error: ", e); // log full stack trace

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(new Date());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setPath(request.getDescription(false).replace("uri=", ""));
        error.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        error.setMessage(e.getMessage());

        return error;
    }
    // Xử lý MethodArgumentNotValidException và ConstraintViolationException
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(Exception e, WebRequest request) {
        log.error("Validation exception occurred: ", e);  // Log lỗi
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(new Date());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setPath(request.getDescription(false).replace("uri=", ""));

        String message = e.getMessage();
        if (e instanceof MethodArgumentNotValidException) {
            error.setError("Payload Invalid");
            int start = message.lastIndexOf("[");
            int end = message.lastIndexOf("]");
            message = message.substring(start + 1, end - 1);
            error.setMessage(message);
        } else if (e instanceof ConstraintViolationException) {
            error.setError("Parameter invalid");
            int start = message.indexOf(":");
            message = message.substring(start + 2);
            error.setMessage(message);
        } else if (e instanceof DataIntegrityViolationException) {
            error.setError("Payload invalid");
            int start = message.indexOf("[") + 1;
            int end = message.indexOf("]") - 1;
            message = message.substring(start, end);
            error.setMessage(message);
        } else {
            error.setError("Invalid Data");
            error.setMessage(message);
        }
        return error;
    }

    // Xử lý ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        log.error("Resource not found: ", e);
        return ErrorResponse.builder()
                .message(e.getMessage())
                .timestamp(new Date())
                .status(HttpStatus.NOT_FOUND.value())
                .path(request.getDescription(false).replace("uri", "http://"))
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .build();
    }

    // Xử lý EmailAlreadyExistsException
    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailAlreadyExistsException(EmailAlreadyExistsException e, WebRequest request) {
        log.error("Email already exists: ", e);
        return ErrorResponse.builder()
                .message(e.getMessage())
                .timestamp(new Date())
                .status(HttpStatus.CONFLICT.value())
                .path(request.getDescription(false).replace("uri", "http://"))
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .build();
    }

    // Xử lý InvalidDataException
    @ExceptionHandler(InvalidDataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInvalidException(InvalidDataException e, WebRequest request) {
        log.error("Invalid data exception: ", e);
        return ErrorResponse.builder()
                .message(e.getMessage())
                .timestamp(new Date())
                .status(HttpStatus.CONFLICT.value())
                .path(request.getDescription(false).replace("uri", "http://"))
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .build();
    }

    // Xử lý MethodArgumentTypeMismatchException
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerErrorException(Exception e, WebRequest request) {
        log.error("Type mismatch exception: ", e);
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(new Date());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setPath(request.getDescription(false).replace("uri", "http://"));
        error.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        error.setMessage(e.getMessage());
        return error;
    }

    // Xử lý SignatureException
    @ExceptionHandler(SignatureException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleSignatureException(SignatureException e, WebRequest request) {
        log.error("Signature exception: ", e);
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(new Date());
        error.setStatus(HttpStatus.UNAUTHORIZED.value());
        error.setPath(request.getDescription(false).replace("uri", "http://"));
        error.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        error.setMessage(e.getMessage());
        return error;
    }

    @ExceptionHandler(InvalidStatusException.class)
    @ResponseStatus(HttpStatus.CONFLICT)  // Trả về mã lỗi 400 (Bad Request)
    public ErrorResponse handleInvalidStatusException(InvalidStatusException e, WebRequest request) {
        // Log lỗi chi tiết
        log.error("Invalid status error: ", e);

        // Tạo đối tượng ErrorResponse để trả về thông tin lỗi
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(new Date());
        error.setStatus(HttpStatus.CONFLICT.value());
        error.setPath(request.getDescription(false).replace("uri", "http://"));
        // Đặt thông tin về lỗi
        error.setError(HttpStatus.CONFLICT.getReasonPhrase());
        error.setMessage(e.getMessage());  // Thông điệp lỗi chi tiết từ ngoại lệ

        // Trả về thông tin lỗi
        return error;
    }
}
