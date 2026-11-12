package pl.clickdrop.economy.application.exception;

public class CollectionNotFoundException extends RuntimeException {
    public CollectionNotFoundException(String code) {
        super("Collection not found: code=" + code);
    }
}
