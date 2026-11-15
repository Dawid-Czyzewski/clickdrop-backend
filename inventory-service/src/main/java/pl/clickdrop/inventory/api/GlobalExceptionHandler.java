package pl.clickdrop.inventory.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.clickdrop.inventory.application.exception.InvalidSellBatchException;
import pl.clickdrop.inventory.application.exception.InventoryItemNotFoundException;
import pl.clickdrop.inventory.application.exception.SkinUpgradeInvalidTargetException;
import pl.clickdrop.inventory.application.exception.SkinUpgradeNoTargetsException;
import pl.clickdrop.inventory.application.exception.TradeUpInvalidException;
import pl.clickdrop.inventory.application.exception.TradeUpNoTargetsException;
import pl.clickdrop.inventory.application.exception.WalletNotFoundException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleWalletNotFound(WalletNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(InventoryItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleInventoryNotFound(InventoryItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidSellBatchException.class)
    public ResponseEntity<Map<String, String>> handleInvalidSellBatch(InvalidSellBatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(SkinUpgradeNoTargetsException.class)
    public ResponseEntity<Map<String, String>> handleNoTargets(SkinUpgradeNoTargetsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(SkinUpgradeInvalidTargetException.class)
    public ResponseEntity<Map<String, String>> handleInvalidTarget(SkinUpgradeInvalidTargetException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(TradeUpInvalidException.class)
    public ResponseEntity<Map<String, String>> handleTradeUpInvalid(TradeUpInvalidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(TradeUpNoTargetsException.class)
    public ResponseEntity<Map<String, String>> handleTradeUpNoTargets(TradeUpNoTargetsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
