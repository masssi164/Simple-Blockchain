package de.flashyotter.blockchain_node.bootstrap;

import blockchain.core.crypto.CryptoUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import de.flashyotter.blockchain_node.service.PeerService;
import de.flashyotter.blockchain_node.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



/**
 * Executed once the Spring context is ready.
 *   • Ensures the wallet is created/loaded
 *   • Starts initial peer synchronisation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartupInitializer implements ApplicationRunner {

    private final WalletService walletSvc;
    private final PeerService   peerSvc;

    @Override
    public void run(ApplicationArguments args) {
        // log wallet address to make field “used”
        String pub = CryptoUtils.keyToBase64(walletSvc.getLocalWallet().getPublicKey());
        log.info("Local address  →  {}", pub);

        log.info("✔  Bootstrap complete – starting P2P sync …");
        peerSvc.init();
    }
}