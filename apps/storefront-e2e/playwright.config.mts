import { defineConfig, devices } from '@playwright/test';
import { nxE2EPreset } from '@nx/playwright/preset';
import { getReadyUrl, workspaceRoot } from '@nx/devkit';

// getReadyUrl moves the address to the port the preview task reported.
const baseURL = process.env['BASE_URL'] || getReadyUrl('http://localhost:4173');

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  ...nxE2EPreset(import.meta.dirname, { testDir: './src' }),
  use: {
    baseURL,
    trace: 'on-first-retry',
  },
  // Nx runs this command as a task before the tests, because it is a plain
  // `nx run <project>:<target>` and reuseExistingServer is true (also in CI).
  // storefront:preview builds the storefront and starts products-api:serve,
  // so one server task covers the whole stack. Don't add `env` or `wait`
  // here: either one makes Nx leave the server to Playwright, and the
  // per-spec @nx-depends-on scoping stops dropping the server's inputs.
  webServer: {
    command: 'pnpm exec nx run storefront:preview',
    url: getReadyUrl('http://localhost:4173'),
    reuseExistingServer: true,
    cwd: workspaceRoot,
  },
  projects: [
    // CI installs only the Chromium headless shell (--only-shell in
    // .nx/pipelines/ci.yaml). Setting channel: 'chrome' or running headed
    // needs the full Chrome build.
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
