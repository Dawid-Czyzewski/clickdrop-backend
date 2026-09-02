package pl.clickdrop.auth.api.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
