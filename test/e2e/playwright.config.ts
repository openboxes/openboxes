import { defineConfig, devices } from '@playwright/test';
import * as path from 'path';
import { APP_URL, HTTP_PORT, WORK_DIR } from './lib/ts/env';

/**
 * Projects:
 *   setup       seeds the API-created data (depots) and saves the browser sign-in (tests/setup)
 *   mobile-api  one test per mobile contract entry against the live server (tests/mobile-api)
 *   flows       warehouse flows end to end, browser + API (tests/flows)
 * Reports: build/e2e/reports/{junit.xml,html/,contract-coverage.json}. Traces on failure.
 * Network: lib/ts/test.ts seals every page (lib/ts/cdn-mirror.ts): the app's and the label stub's
 * loopback ports, OBAF's CDN assets from the CDN mirror (cdn-mirror/cache), nothing else; Chromium's
 * host resolver resolves no name but 127.0.0.1/localhost (launchOptions below); API clients refuse
 * any other URL. Report: cdn-mirror.{json,txt}.
 */
const REPORTS = process.env.E2E_REPORTS_DIR || path.join(WORK_DIR, 'reports');
export const STORAGE_STATE = path.join(WORK_DIR, `admin-state-${HTTP_PORT}.json`);

export default defineConfig({
  testDir: './tests',
  outputDir: path.join(REPORTS, 'test-results'),
  timeout: 90_000,
  expect: { timeout: 15_000 },
  fullyParallel: false,
  workers: Number(process.env.E2E_WORKERS || 2),
  retries: 0,
  forbidOnly: !!process.env.CI,
  reporter: [
    ['list'],
    ['junit', { outputFile: path.join(REPORTS, 'junit.xml') }],
    ['html', { outputFolder: path.join(REPORTS, 'html'), open: 'never' }],
    ['./lib/ts/coverage-reporter.ts', { outputFile: path.join(REPORTS, 'contract-coverage.json') }],
    ['./lib/ts/cdn-mirror-report.ts', { outputFile: path.join(REPORTS, 'cdn-mirror.json') }],
  ],
  use: {
    baseURL: APP_URL + '/',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'off',
    serviceWorkers: 'block',
    ...devices['Desktop Chrome'],
    viewport: { width: 1440, height: 900 },
    // seal below the request routing: every host name except the loopback ones resolves to nothing
    launchOptions: { args: ['--host-resolver-rules=MAP * ~NOTFOUND, EXCLUDE 127.0.0.1, EXCLUDE localhost'] },
  },
  projects: [
    { name: 'setup', testDir: './tests/setup' },
    { name: 'mobile-api', testDir: './tests/mobile-api', dependencies: ['setup'] },
    { name: 'flows', testDir: './tests/flows', dependencies: ['setup'], use: { storageState: STORAGE_STATE } },
  ],
});
