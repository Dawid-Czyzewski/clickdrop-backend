package pl.clickdrop.economy.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.clickdrop.economy.application.exception.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleExists(WalletAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(WalletNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ClickRateLimitException.class)
    public ResponseEntity<Map<String, String>> handleRateLimit(ClickRateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UpgradeNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUpgradeNotFound(UpgradeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UpgradeLevelTooLowException.class)
    public ResponseEntity<Map<String, String>> handleUpgradeLevelTooLow(UpgradeLevelTooLowException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "message", ex.getMessage(),
                        "requiredLevel", String.valueOf(ex.getRequiredLevel()),
                        "userLevel", String.valueOf(ex.getUserLevel())
                ));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientFunds(InsufficientFundsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCaseNotFound(CaseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CaseLevelTooLowException.class)
    public ResponseEntity<Map<String, String>> handleCaseLevelTooLow(CaseLevelTooLowException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "message", ex.getMessage(),
                        "requiredLevel", String.valueOf(ex.getRequiredLevel()),
                        "userLevel", String.valueOf(ex.getUserLevel())
                ));
    }

    @ExceptionHandler(CaseOpenCountLevelTooLowException.class)
    public ResponseEntity<Map<String, String>> handleCaseOpenCountLevelTooLow(
            CaseOpenCountLevelTooLowException ex
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "message", ex.getMessage(),
                        "requestedCount", String.valueOf(ex.getRequestedCount()),
                        "requiredLevel", String.valueOf(ex.getRequiredLevel()),
                        "userLevel", String.valueOf(ex.getUserLevel())
                ));
    }

    @ExceptionHandler(InventoryItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleInventoryNotFound(InventoryItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CaseDropPoolEmptyException.class)
    public ResponseEntity<Map<String, String>> handleEmptyPool(CaseDropPoolEmptyException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(DailyAlreadyClaimedException.class)
    public ResponseEntity<Map<String, String>> handleDailyClaimed(DailyAlreadyClaimedException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of(
                        "message", ex.getMessage(),
                        "nextClaimAt", ex.getNextClaimAt().toString()
                ));
    }

    @ExceptionHandler(CaseOpenRateLimitException.class)
    public ResponseEntity<Map<String, String>> handleCaseOpenRateLimit(CaseOpenRateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(SkinUpgradeNoTargetsException.class)
    public ResponseEntity<Map<String, String>> handleNoTargets(SkinUpgradeNoTargetsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(SkinUpgradeInvalidTargetException.class)
    public ResponseEntity<Map<String, String>> handleInvalidTarget(SkinUpgradeInvalidTargetException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidSellBatchException.class)
    public ResponseEntity<Map<String, String>> handleInvalidSellBatch(InvalidSellBatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCaseOpenCountException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCaseOpenCount(
            InvalidCaseOpenCountException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(TradeUpInvalidException.class)
    public ResponseEntity<Map<String, String>> handleTradeUpInvalid(TradeUpInvalidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(TradeUpNoTargetsException.class)
    public ResponseEntity<Map<String, String>> handleTradeUpNoTargets(TradeUpNoTargetsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CollectionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCollectionNotFound(CollectionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CollectionIncompleteException.class)
    public ResponseEntity<Map<String, String>> handleCollectionIncomplete(CollectionIncompleteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage(), "code", "COLLECTION_INCOMPLETE"));
    }

    @ExceptionHandler(CollectionAlreadyClaimedException.class)
    public ResponseEntity<Map<String, String>> handleCollectionAlreadyClaimed(
            CollectionAlreadyClaimedException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage(), "code", "COLLECTION_ALREADY_CLAIMED"));
    }

    @ExceptionHandler(InvalidBattleRoundsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidBattleRounds(InvalidBattleRoundsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(BattleNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBattleNotFound(BattleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(BattleNotJoinableException.class)
    public ResponseEntity<Map<String, String>> handleBattleNotJoinable(BattleNotJoinableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }
}


