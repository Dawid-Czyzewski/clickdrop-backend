package pl.clickdrop.auth.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.clickdrop.auth.application.exception.EmailAlreadyExistsException;
import pl.clickdrop.auth.application.exception.InvalidCredentialsException;
import pl.clickdrop.auth.application.exception.UsernameAlreadyExistsException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(
            InvalidCredentialsException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid email or password"));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailExists(
            EmailAlreadyExistsException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Email already in use"));
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUsernameExists(
            UsernameAlreadyExistsException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Username already in use"));
    }
}
