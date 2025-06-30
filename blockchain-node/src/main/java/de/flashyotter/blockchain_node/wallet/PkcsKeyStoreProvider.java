package de.flashyotter.blockchain_node.wallet;

import de.flashyotter.blockchain_node.config.NodeProperties;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Optional;

@Component
@Profile("!test")
@Slf4j
public class PkcsKeyStoreProvider implements KeyStoreProvider {

    private final char[] password;
    private final Path storePath;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public PkcsKeyStoreProvider(NodeProperties props,
                               @Value("${wallet.store-path}") String storePath) {
        if (props.getWalletPassword() == null || props.getWalletPassword().isBlank()) {
            throw new IllegalArgumentException("node.walletPassword must be set");
        }
        this.password  = props.getWalletPassword().toCharArray();
        this.storePath = Paths.get(System.getProperty("user.home"), storePath);
    }

    @Override
    public Optional<PrivateKey> load(String alias) throws GeneralSecurityException {
        if (!Files.exists(storePath)) return Optional.empty();
        try (InputStream in = Files.newInputStream(storePath)) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(in, password);
            Key key = ks.getKey(alias, password);
            if (key instanceof PrivateKey pk) {
                return Optional.of(pk);
            }
            return Optional.empty();
        } catch (IOException | CertificateException e) {
            throw new GeneralSecurityException("Failed to load keystore", e);
        }
    }

    /** Load the stored certificate’s public key for a given alias. */
    public PublicKey loadPublicKey(String alias) throws GeneralSecurityException {
        if (!Files.exists(storePath)) {
            throw new GeneralSecurityException("Keystore not found");
        }
        try (InputStream in = Files.newInputStream(storePath)) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(in, password);
            Certificate cert = ks.getCertificate(alias);
            if (cert instanceof X509Certificate) {
                return cert.getPublicKey();
            }
            throw new GeneralSecurityException("No certificate for alias " + alias);
        } catch (IOException | CertificateException e) {
            throw new GeneralSecurityException("Failed to load public key", e);
        }
    }

    @Override
    public void save(String alias, PrivateKey priv) throws GeneralSecurityException {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            if (Files.exists(storePath)) {
                try (InputStream in = Files.newInputStream(storePath)) {
                    ks.load(in, password);
                }
            } else {
                ks.load(null, password);
            }

            // derive pub from priv via correct utility
            PublicKey pub = blockchain.core.crypto.CryptoUtils.derivePublicKey(priv);

            // build self-signed certificate
            X500Name dn       = new X500Name("CN=SimpleChain Wallet");
            BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
            Date notBefore    = new Date(System.currentTimeMillis() - 24L*60*60*1000);
            Date notAfter     = new Date(System.currentTimeMillis() + 10L*365*24*60*60*1000);

            var certBuilder = new JcaX509v3CertificateBuilder(
                    dn, serial, notBefore, notAfter, dn, pub);

            ContentSigner signer;
            try {
                signer = new JcaContentSignerBuilder("SHA256withECDSA")
                             .setProvider("BC")
                             .build(priv);
            } catch (OperatorCreationException oce) {
                throw new GeneralSecurityException("Failed to create content signer", oce);
            }

            X509Certificate cert = new JcaX509CertificateConverter()
                                        .setProvider("BC")
                                        .getCertificate(certBuilder.build(signer));
            Certificate[] chain = new Certificate[]{cert};

            // store into keystore
            ks.setKeyEntry(alias, priv, password, chain);

            // write file
            Files.createDirectories(storePath.getParent());
            try (OutputStream out = Files.newOutputStream(storePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                ks.store(out, password);
            }
        } catch (IOException | CertificateException e) {
            throw new GeneralSecurityException("Failed to save keystore", e);
        }
    }
}
