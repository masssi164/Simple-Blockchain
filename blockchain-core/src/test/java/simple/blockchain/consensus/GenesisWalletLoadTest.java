package simple.blockchain.consensus;

import blockchain.core.consensus.Chain;
import blockchain.core.crypto.AddressUtils;
import blockchain.core.model.TxOutput;
import blockchain.core.model.Wallet;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenesisWalletLoadTest {
    @AfterEach
    void reset() {
        Chain.loadGenesisWallet(null);
    }

    @Test
    void genesisWalletLoadedFromKeystore() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Path ksFile = Files.createTempFile("genesis", ".p12");

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", "BC");
        kpg.initialize(new ECGenParameterSpec("secp256k1"), new SecureRandom());
        KeyPair kp = kpg.generateKeyPair();

        X500Name dn = new X500Name("CN=Genesis");
        BigInteger serial = BigInteger.ONE;
        Date now = new Date();
        var builder = new JcaX509v3CertificateBuilder(dn, serial, now,
                new Date(now.getTime() + 86400000L), dn, kp.getPublic());
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withECDSA")
                .setProvider("BC").build(kp.getPrivate());
        X509Certificate cert = new JcaX509CertificateConverter()
                .setProvider("BC").getCertificate(builder.build(signer));

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, new char[0]);
        ks.setKeyEntry("g", kp.getPrivate(), new char[0], new java.security.cert.Certificate[]{cert});
        try (OutputStream out = Files.newOutputStream(ksFile)) {
            ks.store(out, new char[0]);
        }

        Chain.loadGenesisWallet(ksFile);
        Chain chain = new Chain();

        String addr = AddressUtils.publicKeyToAddress(kp.getPublic());
        Map<String, TxOutput> utxo = chain.getUtxoSnapshot();
        assertEquals(1, utxo.size());
        assertEquals(addr, utxo.values().iterator().next().recipientAddress());
    }
}
