The `ui` directory contains a Vite + React front-end used to interact with the node.

Key files:
- `index.html` – main HTML page for development and production.
- `package.json` / `package-lock.json` – Node dependencies and scripts.
- `build.gradle` – helper tasks for running `npm` from Gradle.
- `vite.config.ts` – Vite build configuration.
- `src/` – TypeScript sources and tests.
- `tsconfig*.json` – TypeScript compiler settings.
- `Dockerfile` packages the UI for production using nginx.
- `nginx.conf` customizes the nginx container.
- `postcss.config.cjs` and `tailwind.config.cjs` provide styling presets.

The UI communicates with the node via REST and a WebSocket helper in
`src/api/ws.ts`. Toast notifications are centralised in `src/services/messageService.tsx`.

Run `npm install` then `npm run dev` in this directory to start the dev server.
