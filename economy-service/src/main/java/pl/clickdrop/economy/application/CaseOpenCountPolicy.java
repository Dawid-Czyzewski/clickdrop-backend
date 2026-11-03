package pl.clickdrop.economy.application;

public final class CaseOpenCountPolicy {

    public static final int MIN_OPEN_COUNT = 1;
    public static final int MAX_OPEN_COUNT = 5;

    public static final int OPEN_X3_MIN_LEVEL = 5;
    public static final int OPEN_X5_MIN_LEVEL = 10;

    private CaseOpenCountPolicy() {}

    public static int requiredLevelForCount(int opens) {
        if (opens >= 5) {
            return OPEN_X5_MIN_LEVEL;
        }
        if (opens >= 3) {
            return OPEN_X3_MIN_LEVEL;
        }
        return 1;
    }

    public static int maxOpenCountForLevel(int userLevel) {
        if (userLevel >= OPEN_X5_MIN_LEVEL) {
            return 5;
        }
        if (userLevel >= OPEN_X3_MIN_LEVEL) {
            return 3;
        }
        return 1;
    }
}
