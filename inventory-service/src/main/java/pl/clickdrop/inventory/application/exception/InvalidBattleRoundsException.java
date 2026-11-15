package pl.clickdrop.inventory.application.exception;

public class InvalidBattleRoundsException extends RuntimeException {
    public InvalidBattleRoundsException() {
        super("Battle rounds must be 1, 3, or 5");
    }
}
