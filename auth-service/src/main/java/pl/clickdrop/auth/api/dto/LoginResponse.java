package pl.clickdrop.auth.api.dto;

import pl.clickdrop.auth.domain.user.Role;

public record LoginResponse(
        Long id,
        String email,
        Role role,
        String token,
        String refreshToken
) { }
