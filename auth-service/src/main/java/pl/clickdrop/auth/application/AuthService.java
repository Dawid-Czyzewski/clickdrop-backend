package pl.clickdrop.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.clickdrop.auth.api.dto.LoginRequest;
import pl.clickdrop.auth.api.dto.LoginResponse;
import pl.clickdrop.auth.api.dto.RegisterRequest;
import pl.clickdrop.auth.api.dto.RegisterResponse;
import pl.clickdrop.auth.application.exception.EmailAlreadyExistsException;
import pl.clickdrop.auth.application.exception.InvalidCredentialsException;
import pl.clickdrop.auth.application.exception.UsernameAlreadyExistsException;
import pl.clickdrop.auth.application.security.JwtService;
import pl.clickdrop.auth.domain.user.Role;
import pl.clickdrop.auth.domain.user.User;
import pl.clickdrop.auth.domain.user.UserRepository;

import java.math.BigDecimal;

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

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException();
        }

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setDollars(BigDecimal.ZERO);
        user.setLevel(1);
        user.setExperience(0);

        User saved = userRepository.save(user);
        return new RegisterResponse(saved.getId(), saved.getEmail(), saved.getUsername(), saved.getRole());
    }
}
