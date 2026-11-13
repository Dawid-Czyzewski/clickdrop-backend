package pl.clickdrop.economy.application.exception;

public class BattleNotJoinableException extends RuntimeException {
    public BattleNotJoinableException(Long battleId) {
        super("Battle is not joinable: id=" + battleId);
    }
}
