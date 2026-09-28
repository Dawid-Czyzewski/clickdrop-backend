package pl.clickdrop.auth.api.dto;

public record ExperienceResponse(
        Integer level,
        Integer experience,
        Integer experienceGained,
        boolean leveledUp
) {
}
