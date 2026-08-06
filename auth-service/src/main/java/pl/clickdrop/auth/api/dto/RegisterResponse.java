package pl.clickdrop.auth.api.dto;

import pl.clickdrop.auth.domain.user.Role;

public record RegisterResponse(
       Long id,
       String email,
       String username,
       Role role
) { }
