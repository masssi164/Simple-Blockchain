Source code for the web front-end.

Folders:
- `api/` – gRPC (`grpc.ts`) and polling/event helpers (`ws.ts`).
- `components/` – React components: `MiningArea`, `Transfer`, `StatCard`, `WalletView`.
- `pages/` – top level views, currently just `Dashboard.tsx`.
- `types/` – shared TypeScript interfaces.
- `assets/` – static assets such as `react.svg`.
- `__tests__/` – unit tests for components and pages.

- `services/` – generated gRPC client bindings.

Entry point `main.tsx` bootstraps React.
