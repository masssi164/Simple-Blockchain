package blockchain.core.model;

import blockchain.core.exceptions.BlockchainException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simplest non-custodial wallet: generates ECDSA key-pair and can
 * aggregate UTXOs to craft & sign a new {@link Transaction}.
 */
@Slf4j @Getter
public class Wallet {

    private final PrivateKey privateKey;
    private final PublicKey  publicKey;

    public Wallet() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
            KeyPair kp = kpg.generateKeyPair();
            privateKey = kp.getPrivate();
            publicKey  = kp.getPublic();
        } catch (GeneralSecurityException e) {
            throw new BlockchainException("Key-pair generation failed", e);
        }
    }

    /**
     * Crafts a signed TX sending {@code amount} to {@code to}.
     *
     * @param utxo Global UTXO set (read-only).  Wallet selects its own outputs.
     */
    public Transaction sendFunds(PublicKey to, double amount, Map<String, TxOutput> utxo) {

        double gathered = 0;
        List<String> used = new ArrayList<>();

        /* gather own UTXOs until >= amount */
        for (var e : utxo.entrySet()) {
            if (e.getValue().recipient().equals(publicKey)) {
                gathered += e.getValue().value();
                used.add(e.getKey());
                if (gathered >= amount) break;
            }
        }

        if (gathered < amount) {
            log.warn("Wallet {}: balance {} < {} – abort", this, gathered, amount);
            throw new BlockchainException("Insufficient funds");
        }

        Transaction tx = new Transaction();
        used.forEach(id -> tx.getInputs().add(new TxInput(id, null, publicKey)));

        tx.getOutputs().add(new TxOutput(amount, to));              // payment
        if (gathered > amount)
            tx.getOutputs().add(new TxOutput(gathered - amount, publicKey)); // change

        tx.signInputs(privateKey);
        return tx;
    }
}
