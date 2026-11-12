package pl.clickdrop.economy.application.exception;

public class CollectionAlreadyClaimedException extends RuntimeException {
    public CollectionAlreadyClaimedException(String code) {
        super("COLLECTION_ALREADY_CLAIMED: code=" + code);
    }
}
