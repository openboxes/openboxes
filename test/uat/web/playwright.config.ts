import { defineConfig, devices } from '@playwright/test';
import * as path from 'path';
import { APP_URL, REPO_ROOT } from '../../e2e/lib/ts/env';

/**
 * Web UAT: browser acceptance flows against the sealed e2e stack. Run through test/uat/run.sh, which
 * brings the stack up and seeds it (test/e2e/up.sh, test/e2e/seed.sh) and creates the read-only audit
 * database user.
 *
 * Evidence for EVERY test, pass or fail: video, trace, a screenshot per named step (lib/uat.ts `step`)
 * and the final screen. Reports: build/uat/reports/{html/,junit.xml,summary.txt,cdn-mirror.{json,txt},
 * test-results/}. Third-party CDN assets are served from the CDN mirror (test/e2e/README.md).
 */
const REPORTS = process.env.UAT_REPORTS_DIR || path.join(REPO_ROOT, 'build', 'uat', 'reports');

export default defineConfig({
  testDir: './tests',
  outputDir: path.join(REPORTS, 'test-results'),
  timeout: 180_000,
  expect: { timeout: 15_000 },
  // flows are isolated by their own data; one worker keeps the machine light and the videos readable
  fullyParallel: false,
  workers: Number(process.env.UAT_WORKERS || 1),
  // a flake is investigated, never retried away
  retries: 0,
  forbidOnly: true,
  reporter: [
    ['list'],
    ['junit', { outputFile: path.join(REPORTS, 'junit.xml') }],
    ['html', { outputFolder: path.join(REPORTS, 'html'), open: 'never' }],
    ['../../e2e/lib/ts/cdn-mirror-report.ts', { outputFile: path.join(REPORTS, 'cdn-mirror.json') }],
  ],
  use: {
    baseURL: APP_URL + '/',
    trace: 'on',
    video: 'on',
    screenshot: 'on',
    serviceWorkers: 'block',
    ...devices['Desktop Chrome'],
    viewport: { width: 1440, height: 900 },
    // seal below the request routing: every host name except the loopback ones resolves to nothing
    launchOptions: { args: ['--host-resolver-rules=MAP * ~NOTFOUND, EXCLUDE 127.0.0.1, EXCLUDE localhost'] },
  },
  projects: [{ name: 'web', testDir: './tests' }],
});
