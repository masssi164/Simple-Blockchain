import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    setupFiles: './src/setupTests.ts',
    globals: true,               // let you use "describe/it/expect" everywhere
    coverage: {
      reporter: ['text', 'html'],  // requires c8
    },
  },
});

  
