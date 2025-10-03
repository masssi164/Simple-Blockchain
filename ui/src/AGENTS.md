Source code for the web front-end.

Folders:
- `api/` – JSON-RPC helper (`jsonRpc.ts`) and polling/event helpers (`ws.ts`).
- `components/` – React components: `MiningArea`, `Transfer`, `StatCard`, `WalletView`.
- `pages/` – top level views, currently just `Dashboard.tsx`.
- `types/` – shared TypeScript interfaces.
- `assets/` – static assets such as `react.svg`.
- `__tests__/` – unit tests for components and pages.

- `services/` – shared helpers such as the toast `messageService`.

Entry point `main.tsx` bootstraps React.
