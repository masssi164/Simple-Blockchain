package de.flashyotter.blockchain_node.controler;

import blockchain.core.exceptions.BlockchainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates domain errors into proper HTTP status codes that the UI / tests
 * can assert against.
 */
@RestControllerAdvice
public class RestErrorHandler {

    @ExceptionHandler(BlockchainException.class)
    public ResponseEntity<String> onBlockchainError(BlockchainException ex) {
        // 409 ➜ semantic conflict with current chain/UTXO state
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
