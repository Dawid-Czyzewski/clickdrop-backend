package pl.clickdrop.inventory.application.exception;

public class CaseNotFoundException extends RuntimeException {
    public CaseNotFoundException(Long caseId) {
        super("Case not found id=" + caseId);
    }
}
