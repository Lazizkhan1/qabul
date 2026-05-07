package uz.umft.qabul.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public AuthException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
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

    public static AuthException forbidden(String code, String message) {
        return new AuthException(code, message, HttpStatus.FORBIDDEN);
    }

    public static AuthException notFound(String code, String message) {
        return new AuthException(code, message, HttpStatus.NOT_FOUND);
    }
}
