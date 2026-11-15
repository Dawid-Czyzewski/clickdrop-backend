package pl.clickdrop.inventory.application.exception;

public class CaseLevelTooLowException extends RuntimeException {

    private final Long caseId;
    private final int requiredLevel;
    private final int userLevel;

    public CaseLevelTooLowException(Long caseId, int requiredLevel, int userLevel) {
        super("Case requires level " + requiredLevel + ", user has " + userLevel);
        this.caseId = caseId;
        this.requiredLevel = requiredLevel;
        this.userLevel = userLevel;
    }

    public Long getCaseId() {
        return caseId;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public int getUserLevel() {
        return userLevel;
    }
}
