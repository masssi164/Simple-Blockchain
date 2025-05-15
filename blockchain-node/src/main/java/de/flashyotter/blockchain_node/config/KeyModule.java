package de.flashyotter.blockchain_node.config;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class KeyModule extends SimpleModule {

    public KeyModule() {
        super("KeyModule");
        addSerializer(PublicKey.class, new JsonSerializer<>() {
            @Override
            public void serialize(PublicKey key, JsonGenerator gen, SerializerProvider p)
                    throws java.io.IOException {
                gen.writeString(Base64.getEncoder().encodeToString(key.getEncoded()));
            }
        });
        addDeserializer(PublicKey.class, new JsonDeserializer<>() {
            @Override
            public PublicKey deserialize(JsonParser jp, DeserializationContext ctxt)
                    throws java.io.IOException, JsonProcessingException {

                if (jp.currentToken() != JsonToken.VALUE_STRING)
                    throw ctxt.wrongTokenException(jp, PublicKey.class, JsonToken.VALUE_STRING, "Base64 key expected");

                try {
                    byte[] bytes = Base64.getDecoder().decode(jp.getText());
                    var spec  = new X509EncodedKeySpec(bytes);
                    return KeyFactory.getInstance("EC").generatePublic(spec);
                } catch (Exception e) {
                    throw new JsonMappingException(jp, "Cannot read EC public key", e);
                }
            }
        });
    }
}
