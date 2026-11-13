package pl.clickdrop.economy.application.exception;

public class BattleNotFoundException extends RuntimeException {
    public BattleNotFoundException(Long battleId) {
        super("Battle not found: id=" + battleId);
    }
}
