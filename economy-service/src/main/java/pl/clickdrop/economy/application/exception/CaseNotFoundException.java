package pl.clickdrop.economy.application.exception;

public class CaseNotFoundException extends RuntimeException {
    public CaseNotFoundException(Long caseId) {
        super("Case not found id=" + caseId);
    }
}
