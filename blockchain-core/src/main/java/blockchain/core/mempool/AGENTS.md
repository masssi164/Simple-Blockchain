In-memory queue for pending transactions.

- `Mempool.java` — thread-safe storage with validation helpers.

Errors
------
- `[LOGIC_BAD_CHECK]` - `calcFee` allows transactions with outputs exceeding inputs, yielding negative fees.
