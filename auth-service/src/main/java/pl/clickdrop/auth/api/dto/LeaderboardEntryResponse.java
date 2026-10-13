package pl.clickdrop.auth.api.dto;

public record LeaderboardEntryResponse(
        int rank,
        String username,
        Integer level,
        Integer experience
) {
}
