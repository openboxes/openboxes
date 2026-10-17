/**
 * The suite's `test`: Playwright's, with
 *  - every browser context sealed (lib/ts/cdn-mirror.ts): the app's and the label stub's loopback ports continue, OBAF's third-party CDN
 *    assets are served from the CDN mirror (cdn-mirror/cache), anything else is aborted; per page, what was served and
 *    what was missing from the mirror is attached for the report (E2E_STRICT_EGRESS=1 fails a test
 *    whose pages made an aborted request);
 *  - `fx` (fixtures, an admin session in Main Warehouse) and `covers()` (contract coverage annotation);
 *  - a worker-scoped automatic fixture that closes the shared database pool when the worker ends.
 */
import { test as base, expect } from '@playwright/test';
import { MirrorLog, routeSealed } from './cdn-mirror';
import { Fixtures } from './fixtures';
import { closeDb } from './db';

type Extra = { fx: Fixtures; mirror: MirrorLog };
type WorkerExtra = { dbWorker: void };

export const test = base.extend<Extra, WorkerExtra>({
  dbWorker: [async ({}, use) => {
    await use();
    await closeDb();
  }, { scope: 'worker', auto: true }],
  mirror: async ({}, use) => { await use(new MirrorLog()); },
  context: async ({ context, mirror }, use) => {
    await context.route('**/*', (route) => routeSealed(route, mirror));
    await use(context);
    // Recorded, not failed: a third-party URL missing from the mirror is a finding (README.md "CDN
    // mirror"), and E2E_STRICT_EGRESS=1 turns any aborted request into a failure.
    await mirror.attach(test.info());
    if (process.env.E2E_STRICT_EGRESS) expect(mirror.aborted(), 'requests to non-loopback hosts were aborted (missing from the mirror or excluded)').toEqual([]);
  },
  fx: async ({}, use) => {
    const fx = await Fixtures.create();
    await use(fx);
    await fx.dispose();
  },
});

type Body = Parameters<typeof test>[2];
const ann = (ids: string | string[]) => (Array.isArray(ids) ? ids : [ids]).map((d) => ({ type: 'covers', description: d }));

/**
 * A contract test: `ids` are the contract entries it exercises (read by lib/ts/coverage-reporter.ts).
 * `characterise` marks a test that pins what the server answers TODAY where that is not what the
 * app expects (next to the KNOWN-GAP test asserting the expected answer): it exercises the entry
 * but does not count as conformant.
 */
export function contractTest(title: string, ids: string | string[], body: Body, opts: { characterise?: string } = {}): void {
  const annotation = ann(ids);
  if (opts.characterise) annotation.push({ type: 'characterised', description: opts.characterise });
  test(title, { annotation }, body);
}

/**
 * A KNOWN-GAP test: it asserts the CORRECT behaviour and is `test.fixme` while the gap is open; `gap`
 * describes the symptom and where it comes from. E2E_KNOWN_GAPS=1 runs every gap as a normal test: an
 * open gap fails for its reason, a fixed one passes (turn it into a contractTest then).
 */
export function knownGap(title: string, gap: string, ids: string | string[], body: Body): void {
  const details = { annotation: [...ann(ids), { type: 'KNOWN-GAP', description: gap }] };
  const t = `${title} [KNOWN-GAP]`;
  if (process.env.E2E_KNOWN_GAPS) test(t, details, body);
  else test.fixme(t, details, body);
}

export { expect };
