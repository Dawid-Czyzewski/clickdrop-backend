package pl.clickdrop.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.clickdrop.auth.api.dto.LoginRequest;
import pl.clickdrop.auth.api.dto.LoginResponse;
import pl.clickdrop.auth.application.exception.InvalidCredentialsException;
import pl.clickdrop.auth.application.security.JwtService;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email()).
                orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(user.getId(), user.getEmail(), user.getRole(), token);
    }
}
