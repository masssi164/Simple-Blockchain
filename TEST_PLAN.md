# Testarchitektur

Dieser Plan fasst die End-to-End und Regressionstests zusammen, die die alte GitHub-Pipeline ersetzen.

## Aufbau
- **Integrationstests**
  - *blockchain-node*: startet den Spring Boot Kontext und ruft `/actuator/health` auf.
  - *ui*: führt den Produktionsbuild mittels `npm run build` aus.
- **E2E-Tests**
  - Behave Features unter `pipeline-tests/` orchestrieren zwei Container sowie Selenium und pruefen Block-Synchronisation.
- **Regressionstests**
  - Das Feature `regression.feature` stellt sicher, dass `./gradlew` und `npm` Befehle funktionieren.

## Wichtige Mechanismen
1. Starten und Health-Check des Nodes.
2. Erfolgreicher Vite/TypeScript Build der UI.
3. Synchronisation zweier Nodes inklusive Mining und Transaktionen.
4. Ausfuehren von `./gradlew help` sowie `npm test` als Sanity-Check fuer die Toolchain.

Die Tests werden lokal mit `behave` oder ueber die Gradle/Vitest Tasks ausgefuehrt.
- **Auth-Tests**
  - Integrationstest prueft, dass REST Endpunkte ein gueltiges JWT benoetigen.
- **gRPC-Tests**
  - Testet Wallet- und Chain-Service ueber einen in-memory GRPC Server.
- **P2P-Tests**
  - Stellt sicher, dass Libp2p Nachrichten mit falschem Token verworfen werden.
