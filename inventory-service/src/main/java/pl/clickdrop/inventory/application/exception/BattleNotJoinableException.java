package pl.clickdrop.inventory.application.exception;

public class BattleNotJoinableException extends RuntimeException {
    public BattleNotJoinableException(Long battleId) {
        super("Battle is not joinable: id=" + battleId);
    }
}
