package pl.clickdrop.economy.application.client;

public record AuthExperienceResponse(
        Integer level,
        Integer experience,
        Integer experienceGained,
        boolean leveledUp
) {
}
