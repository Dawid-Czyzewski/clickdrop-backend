package pl.clickdrop.inventory.application.exception;

public class CollectionIncompleteException extends RuntimeException {
    public CollectionIncompleteException(String code) {
        super("COLLECTION_INCOMPLETE: code=" + code);
    }
}
