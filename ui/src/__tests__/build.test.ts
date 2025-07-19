import { execSync } from 'node:child_process';
import { describe, it, expect } from 'vitest';
import path from 'node:path';

// Runs the production build to ensure the tooling works end-to-end

describe('ui build', () => {
  it('builds without errors', () => {
    const root = path.resolve(__dirname, '..');
    execSync('npm run build -- --emptyOutDir', {
      cwd: root,
      stdio: 'ignore',
      timeout: 120000
    });
    expect(true).toBe(true);
  }, 120000);
});
