# Testarchitektur

Dieser Plan fasst die End-to-End und Regressionstests zusammen, die die alte GitHub-Pipeline ersetzen.

## Aufbau
- **Integrationstests**
  - *blockchain-node*: startet den Spring Boot Kontext und ruft `/actuator/health` auf.
  - *ui*: führt den Produktionsbuild mittels `npm run build` aus.
- **Regressionstests**
  - Python Tests im Verzeichnis `tests` prüfen Docker Compose und die Toolchain.

## Wichtige Mechanismen
1. Starten und Health-Check des Nodes.
2. Erfolgreicher Vite/TypeScript Build der UI.
3. Synchronisation zweier Nodes inklusive Mining und Transaktionen.
4. Ausfuehren von `./gradlew help` sowie `npm test` als Sanity-Check fuer die Toolchain.

Die Tests werden lokal mit `make ci` ausgeführt.
- **Auth-Tests**
  - Integrationstest prueft, dass REST Endpunkte ein gueltiges JWT benoetigen.
- **JSON-RPC-Tests**
  - Decken die `JsonRpcController`-Methoden fuer Mining, Pagination und Wallet-Infos per MockMvc ab.
- **P2P-Tests**
  - Stellt sicher, dass Libp2p Nachrichten mit falschem Token verworfen werden.
