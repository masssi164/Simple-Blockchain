package simple.blockchain.crypto;

import blockchain.core.crypto.AddressUtils;
import blockchain.core.model.Wallet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressUtilsTest {

    @Test
    void publicKeyToAddress_roundTripValidates() {
        Wallet w = new Wallet();
        String addr = AddressUtils.publicKeyToAddress(w.getPublicKey());
        assertTrue(AddressUtils.isValid(addr));
    }

    @Test
    void invalidStringFailsValidation() {
        assertFalse(AddressUtils.isValid("notARealAddress"));
    }
}
