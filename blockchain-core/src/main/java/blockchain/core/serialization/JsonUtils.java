package blockchain.core.serialization;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Wire-format helper – Jackson <→ POJO.
 *
 * <p>❖ 100 % Spring-agnostic (core has ZERO Spring dependencies)<br>
 * ❖ Allows a fully configured ObjectMapper to be “plugged in” at runtime
 *    (e.g. by a Spring Boot app) while still working perfectly in plain
 *    Java tests or command-line tools.</p>
 */
public final class JsonUtils {

    /** ⚠ MUST remain non-final so it can be swapped at runtime. */
    private static volatile ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()               // picks up all Jackson modules on classpath
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    private JsonUtils() {
        /* utility class – no instances */
    }

    /* ---------------------------------------------------------- */
    /* Spring (or any other framework) can call this exactly once */
    /* early during bootstrap to inject its own configured mapper */
    /* ---------------------------------------------------------- */
    public static void use(ObjectMapper external) {
        mapper = external;
    }

    /* ---------------------------------------------------------- */
    /* Convenience helpers for the most common model types        */
    /* ---------------------------------------------------------- */
    public static String toJson(Block block) {
        try {
            return mapper.writeValueAsString(block);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize Block", e);
        }
    }

    public static String toJson(Transaction tx) {
        try {
            return mapper.writeValueAsString(tx);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize Transaction", e);
        }
    }

    public static Block blockFromJson(String json) {
        try {
            return mapper.readValue(json, Block.class);
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize Block", e);
        }
    }

    public static Transaction txFromJson(String json) {
        try {
            return mapper.readValue(json, Transaction.class);
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize Transaction", e);
        }
    }
}
