package blockchain.core.serialization;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Wire-format helper – Jackson <→ POJO.
 * Keeps network layer lightweight by re-using the same model objects.
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    private JsonUtils() { }

    public static String toJson(Block b) {
        try { return MAPPER.writeValueAsString(b); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public static String toJson(Transaction t) {
        try { return MAPPER.writeValueAsString(t); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public static Block blockFromJson(String json) {
        try { return MAPPER.readValue(json, Block.class); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public static Transaction txFromJson(String json) {
        try { return MAPPER.readValue(json, Transaction.class); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
