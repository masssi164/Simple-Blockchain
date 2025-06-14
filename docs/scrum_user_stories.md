# Scrum User Stories

## Fee/priority mempool & eviction

**User Story**

As a miner, I want transactions in the mempool to be sorted by fee so that blocks include the most profitable transactions first. When the mempool is full, lower-fee transactions should be evicted to keep space for higher-paying ones.

**Tasks**

1. Extend `Mempool` to calculate transaction fees and store entries ordered by fee.
2. Add an eviction policy that removes the lowest-fee transaction when the mempool exceeds its capacity.
3. Update unit tests to verify priority ordering and eviction behaviour.

**Definition of Done**

- All tasks implemented and tests pass.
- `./gradlew build` succeeds.
- `npm test` in `ui/` runs without failures.
- The roadmap entry in `README.md` is marked as completed.
