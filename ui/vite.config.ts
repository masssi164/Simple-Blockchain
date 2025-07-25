import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    setupFiles: './src/setupTests.ts',
    globals: true,               // let you use "describe/it/expect" everywhere
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
    },
  },
});

  
