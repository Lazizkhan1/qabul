package uz.umft.qabul.auth;

import org.springframework.http.HttpStatus;

public class AuthException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public AuthException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static AuthException badRequest(String code, String message) {
        return new AuthException(code, message, HttpStatus.BAD_REQUEST);
    }

    public static AuthException unauthorized(String code, String message) {
        return new AuthException(code, message, HttpStatus.UNAUTHORIZED);
    }

    public static AuthException conflict(String code, String message) {
        return new AuthException(code, message, HttpStatus.CONFLICT);
    }
}
