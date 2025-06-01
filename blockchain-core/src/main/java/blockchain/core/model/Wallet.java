package blockchain.core.model;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.exceptions.BlockchainException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;

/** Simple key-pair holder plus convenience sendFunds. */
@Slf4j @Getter
public class Wallet {

    static { Security.addProvider(new BouncyCastleProvider()); }

    private final PrivateKey privateKey;
    private final PublicKey  publicKey;


    public Wallet() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", "BC");
            kpg.initialize(new ECGenParameterSpec("secp256k1"), new SecureRandom());
            KeyPair kp = kpg.generateKeyPair();
            privateKey = kp.getPrivate();
            publicKey  = kp.getPublic();
        } catch (GeneralSecurityException e) {
            throw new BlockchainException("Key-pair generation failed", e);
        }
    }

    public Wallet(PrivateKey priv, PublicKey pub) {
        this.privateKey = priv; this.publicKey = pub;
    }


    /** NEW – address based API used by the node/UI layer. */
    public Transaction sendFunds(String toAddr,
                                 double amount,
                                 Map<String, TxOutput> utxo) {

        String myAddr = AddressUtils.publicKeyToAddress(publicKey);

        double gathered = 0;
        List<String> spendIds = new ArrayList<>();

        for (var e : utxo.entrySet()) {
            TxOutput out = e.getValue();
            if (!myAddr.equals(out.recipientAddress())) continue;
            gathered += out.value();
            spendIds.add(e.getKey());
            if (gathered + 1e-9 >= amount) break;
        }
        if (gathered + 1e-9 < amount)
            throw new BlockchainException("balance too low");

        Transaction tx = new Transaction();
        spendIds.forEach(id ->
                tx.getInputs().add(new TxInput(id, new byte[0], publicKey)));

        tx.getOutputs().add(new TxOutput(amount, toAddr));

        double change = gathered - amount;
        if (change > 1e-9)
            tx.getOutputs().add(new TxOutput(change, myAddr));

        tx.signInputs(privateKey);
        return tx;
    }

    /** Compatibility helper for old PublicKey-based callers. */
    @Deprecated
    public Transaction sendFunds(PublicKey to,
                                 double amount,
                                 Map<String, TxOutput> utxo) {
        return sendFunds(AddressUtils.publicKeyToAddress(to), amount, utxo);
    }
}
