package pl.clickdrop.economy.application.exception;

public class CaseOpenCountLevelTooLowException extends RuntimeException {

    private final int requestedCount;
    private final int requiredLevel;
    private final int userLevel;

    public CaseOpenCountLevelTooLowException(int requestedCount, int requiredLevel, int userLevel) {
        super(
                "Case open ×"
                        + requestedCount
                        + " requires level "
                        + requiredLevel
                        + ", user has "
                        + userLevel
        );
        this.requestedCount = requestedCount;
        this.requiredLevel = requiredLevel;
        this.userLevel = userLevel;
    }

    public int getRequestedCount() {
        return requestedCount;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public int getUserLevel() {
        return userLevel;
    }
}
