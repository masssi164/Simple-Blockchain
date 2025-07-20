Data model classes.

- `Block` and `BlockHeader` — immutable structures with PoW fields.
- `Transaction`, `TxInput`, `TxOutput` — UTXO-based transaction model.
- `Wallet` — simple key pair wrapper.
- `Block.java.agent.md` — explanation of mining helper.

Errors
------
- `[LOGIC_BAD_HASH]` - `Transaction` concatenates fields without delimiters
  when computing the hash, leading to potential collisions.
