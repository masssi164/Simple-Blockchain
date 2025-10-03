package de.flashyotter.blockchain_node.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import de.flashyotter.blockchain_node.service.PeerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Executed once the Spring context is ready.
 *   - Starts initial peer synchronisation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartupInitializer implements ApplicationRunner {

    private final PeerService peerSvc;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Bootstrap complete - starting P2P sync ...");
        peerSvc.init();
    }
}
