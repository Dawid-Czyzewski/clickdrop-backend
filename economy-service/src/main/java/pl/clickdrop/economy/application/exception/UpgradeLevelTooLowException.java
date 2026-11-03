package pl.clickdrop.economy.application.exception;

public class UpgradeLevelTooLowException extends RuntimeException {

    private final Long upgradeId;
    private final int requiredLevel;
    private final int userLevel;

    public UpgradeLevelTooLowException(Long upgradeId, int requiredLevel, int userLevel) {
        super("Upgrade requires level " + requiredLevel + ", user has " + userLevel);
        this.upgradeId = upgradeId;
        this.requiredLevel = requiredLevel;
        this.userLevel = userLevel;
    }

    public Long getUpgradeId() {
        return upgradeId;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public int getUserLevel() {
        return userLevel;
    }
}
