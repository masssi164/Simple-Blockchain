Source code for the web front-end.

Folders:
- `api/` – REST (`rest.ts`) and WebSocket (`ws.ts`) helpers.
- `components/` – React components: `MiningArea`, `Transfer`, `StatCard`, `WalletView`.
- `pages/` – top level views, currently just `Dashboard.tsx`.
- `types/` – shared TypeScript interfaces.
- `assets/` – static assets such as `react.svg`.
- `services/` – client side helpers like `messageService`.
- `__tests__/` – unit tests for components and pages.

WebSocket events from the node are dispatched via `wsSingleton` in `api/ws.ts`.

Entry point `main.tsx` bootstraps React.
