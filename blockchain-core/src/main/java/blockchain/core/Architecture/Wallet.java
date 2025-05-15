package blockchain.core.Architecture;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import blockchain.core.exceptions.BlockchainException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
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
        } catch (Exception e) {
            throw new BlockchainException("Key‑Pair Erzeugung fehlgeschlagen", e);
        }
    }

    public Transaction sendFunds(PublicKey to, double amount, Map<String, TxOutput> utxo) {

        double gathered = 0;
        List<String> used = new ArrayList<>();
        for (var e : utxo.entrySet()) {
            if (e.getValue().getRecipient().equals(publicKey)) {
                gathered += e.getValue().getValue();
                used.add(e.getKey());
                if (gathered >= amount) break;
            }
        }
        if (gathered < amount) {
            log.warn("Wallet {}: Saldo {}  <  {} – Abbruch", this, gathered, amount);
            throw new BlockchainException("Saldo zu klein");
        }

        Transaction tx = new Transaction();
        used.forEach(id -> tx.getInputs().add(new TxInput(id, null, publicKey)));
        tx.getOutputs().add(new TxOutput(amount, to));
        if (gathered > amount)
            tx.getOutputs().add(new TxOutput(gathered - amount, publicKey));

        tx.signInputs(privateKey);
        return tx;
    }
}
