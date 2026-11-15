package pl.clickdrop.inventory.application.exception;

public class CaseDropPoolEmptyException extends RuntimeException {
    public CaseDropPoolEmptyException(Long caseId) {
        super("No drops configured for case id=" + caseId);
    }
}
