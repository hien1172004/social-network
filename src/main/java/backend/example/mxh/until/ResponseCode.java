package backend.example.mxh.until;

public enum ResponseCode {
    SUCCESS(200),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),
    INTERNAL_ERROR(500);

    private final int code;

    ResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
