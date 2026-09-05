package pl.clickdrop.auth.api.dto;

public record UserResponse(
        String username,
        String email,
        Integer level,
        Integer experience
) {
}