package pl.clickdrop.economy.application.exception;

public class CollectionIncompleteException extends RuntimeException {
    public CollectionIncompleteException(String code) {
        super("COLLECTION_INCOMPLETE: code=" + code);
    }
}
