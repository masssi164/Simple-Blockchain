The `ui` directory contains a Vite + React front-end used to interact with the node.

Key files:
- `index.html` – main HTML page for development and production.
- `package.json` / `package-lock.json` – Node dependencies and scripts.
- `build.gradle` – helper tasks for running `npm` from Gradle.
- `vite.config.ts` – Vite build configuration.
- `src/` – TypeScript sources and tests.
- `tsconfig*.json` – TypeScript compiler settings.
- Communicates with the backend via REST and gRPC clients.

Run `npm install` then `npm run dev` in this directory to start the dev server.
