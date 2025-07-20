This package defines consensus logic.

- `Chain.java` — manages the block DAG, difficulty adjustment and reorganisations.
- `ConsensusParams.java` — network-wide constants like block interval and reward schedule.
- `Chain.java.agent.md` — extra notes describing the chain implementation.

Errors
------
- `[LOGIC_BAD_CHECK]` - `Chain.java` uses `MAX_BLOCK_SIZE_BYTES` for both
  byte size and transaction count validation.
