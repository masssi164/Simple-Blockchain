package de.flashyotter.blockchain_node.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import blockchain.core.serialization.JsonUtils;

import jakarta.annotation.PostConstruct;

/**
 * Registers custom Jackson modules for the node project and
 * hands Spring’s fully configured {@link ObjectMapper} over to
 * {@link JsonUtils} so that core and node share the exact same
 * serialization settings.
 */
@Configuration
public class JacksonConfig {

    @org.springframework.beans.factory.annotation.Autowired
    private ObjectMapper mapper;

    
    /* ------------------------------------------------------------------ */
    /* 1) Custom module for (de)serialising java.security.PublicKey       */
    /* ------------------------------------------------------------------ */
    @Bean
    public Module publicKeyModule() {
        SimpleModule m = new SimpleModule();

        // Serializer: PublicKey → Base64
        m.addSerializer(PublicKey.class, new StdSerializer<>(PublicKey.class) {
            @Override
            public void serialize(PublicKey key,
                                  JsonGenerator gen,
                                  SerializerProvider serializers) throws IOException {
                gen.writeString(Base64.getEncoder().encodeToString(key.getEncoded()));
            }
        });

        // Deserializer: Base64 → PublicKey
        m.addDeserializer(PublicKey.class, new StdDeserializer<>(PublicKey.class) {
            @Override
            public PublicKey deserialize(JsonParser p,
                                         DeserializationContext ctxt) throws IOException {
                try {
                    byte[] bytes = Base64.getDecoder().decode(p.getValueAsString());
                    KeyFactory kf = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
                    return kf.generatePublic(new X509EncodedKeySpec(bytes));
                } catch (GeneralSecurityException e) {
                    throw new IOException(e);
                }
            }
        });

        return m;
    }

    /* ------------------------------------------------------------------ */
    /* 2) Hand Spring’s ObjectMapper over to core.JsonUtils               */
    /* ------------------------------------------------------------------ */
    @PostConstruct
    void init() {
        JsonUtils.use(mapper);
    }
}
