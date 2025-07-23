package de.flashyotter.blockchain_node.service;

import blockchain.core.model.Transaction;

import java.util.List;

/**
 * Stub service for transaction-related operations (for testing)
 */
public interface TransactionService {
    List<Transaction> getPendingTransactions();
    boolean submitTransaction(Transaction tx);
}
