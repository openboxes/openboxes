/**
 * The web UAT's `test`: Playwright's test, with
 *
 *   flow / knownGap / smoke   a test's details argument: its kind (tag and annotations);
 *   step(name, fn)        a named screen step: a test.step plus a full-page screenshot after it (also on failure),
 *                         kept as test-results/<test>/step-NN-<name>.png and in the HTML report;
 *   audit(name, fn)       a labelled AUDIT: read-only database comparisons (lib/audit-db.ts), never the page;
 *   startState(facts)     the flow's declared starting state, read back (read only) and attached;
 *   data                  the per-flow unique fixture factory (lib/data.ts), for setup before the first step;
 *   a sealed browser      the app's loopback port, OpenBoxes' third-party CDN assets served from the CDN mirror, nothing
 *                         else (test/e2e/lib/ts/cdn-mirror.ts); per page, served / missing is reported.
 *
 * A known-gap test asserts the correct behaviour of a flow step that does not work yet; it is fixme
 * (not run) unless UAT_KNOWN_GAPS=1 (test/uat/run.sh --known-gaps), so a fixed gap shows up as a pass.
 */
import { test as base, expect, TestInfo } from '@playwright/test';
import { MirrorLog, routeSealed } from '../../../e2e/lib/ts/cdn-mirror';
import { closeDb } from '../../../e2e/lib/ts/db';
import { FlowData } from './data';
import { closeAuditDb, readOnly, ReadOnly } from './audit-db';

type StepFn = <T>(name: string, fn: () => Promise<T>) => Promise<T>;
type AuditFn = (name: string, fn: (db: ReadOnly) => Promise<void>) => Promise<void>;
type Fact = { expect: unknown; read: (db: ReadOnly) => Promise<unknown> };
type StartStateFn = (facts: Record<string, Fact>) => Promise<void>;

/** Per-test bookkeeping shared by the fixtures. */
class UatState {
  stepNo = 0;
  auditing = 0;
  auditViolations: string[] = [];
  /** per page: third-party URLs served from the mirror / missing from it */
  mirror = new MirrorLog();
}

function flowOf(info: TestInfo): string {
  const a = info.annotations.find((x) => x.type === 'uat-flow');
  return a?.description ?? 'SMOKE';
}

export const test = base.extend<{ gapGate: void; uat: UatState; step: StepFn; audit: AuditFn; startState: StartStateFn; data: FlowData }, { uatWorker: void }>({
  uatWorker: [async ({}, use) => {
    await use();
    await closeAuditDb();
    await closeDb();
  }, { scope: 'worker', auto: true }],

  uat: async ({}, use) => { await use(new UatState()); },

  // a known-gap test is not run (fixme) unless UAT_KNOWN_GAPS=1 (test/uat/run.sh --known-gaps)
  gapGate: [async ({}, use, info) => {
    const g = info.annotations.find((a) => a.type === 'KNOWN-GAP');
    if (g && process.env.UAT_KNOWN_GAPS !== '1') info.fixme(true, `KNOWN-GAP ${g.description}: not run (test/uat/run.sh --known-gaps runs it)`);
    await use();
  }, { auto: true }],

  context: async ({ context, uat }, use, info) => {
    await context.route('**/*', (route) => {
      const req = route.request();
      if (uat.auditing > 0 && req.method() !== 'GET') uat.auditViolations.push(`${req.method()} ${req.url()}`);
      return routeSealed(route, uat.mirror);
    });
    await use(context);
    await uat.mirror.attach(info);
    expect(uat.auditViolations, 'the page sent requests while an AUDIT ran (audits never drive the flow)').toEqual([]);
  },

  data: async ({}, use, info) => {
    const d = new FlowData(flowOf(info));
    await use(d);
    if (d.created.length) {
      await info.attach('test data (setup)', { body: JSON.stringify(d.created, null, 2), contentType: 'application/json' });
    }
    await d.dispose();
  },

  step: async ({ page, uat, data }, use, info) => {
    const fn: StepFn = async (name, body) => {
      data.seal();
      const n = String(++uat.stepNo).padStart(2, '0');
      const title = `STEP ${n}: ${name}`;
      const file = (suffix: string) => info.outputPath(`step-${n}-${name.replace(/[^A-Za-z0-9]+/g, '-').replace(/^-|-$/g, '').slice(0, 60)}${suffix}.png`);
      return test.step(title, async () => {
        try {
          const r = await body();
          const f = file('');
          await page.screenshot({ path: f, fullPage: true });
          await info.attach(`${title}.png`, { path: f, contentType: 'image/png' });
          return r;
        } catch (e) {
          try {
            const f = file('-FAILED');
            await page.screenshot({ path: f, fullPage: true });
            await info.attach(`${title} (failed).png`, { path: f, contentType: 'image/png' });
          } catch { /* the page may be gone */ }
          throw e;
        }
      });
    };
    await use(fn);
  },

  audit: async ({ uat }, use, info) => {
    const fn: AuditFn = async (name, body) => {
      info.annotations.push({ type: 'AUDIT', description: name });
      await test.step(`AUDIT: ${name}`, async () => {
        uat.auditing++;
        try { await body(readOnly); } finally { uat.auditing--; }
      });
    };
    await use(fn);
  },

  startState: async ({}, use, info) => {
    const fn: StartStateFn = async (facts) => {
      await test.step('START STATE (declared, read back read-only)', async () => {
        const report: Record<string, { declared: unknown; observed: unknown }> = {};
        for (const [k, f] of Object.entries(facts)) report[k] = { declared: f.expect, observed: await f.read(readOnly) };
        await info.attach('start state', { body: JSON.stringify(report, null, 2), contentType: 'application/json' });
        info.annotations.push({ type: 'start-state', description: Object.keys(facts).join('; ') });
        for (const [k, r] of Object.entries(report)) expect(r.observed, `start state: ${k}`).toEqual(r.declared);
      });
    };
    await use(fn);
  },
});

export { expect };

// ------------------------------------------------------------------ declaring tests
// The kind of a test is its details argument, so Playwright reports the spec file's own line:
//   test('receiving: receive a partial inbound', flow('receiving'), async ({ page, step, audit, data }) => { ... });
//   test('picking: pick a web-wizard order', knownGap('picking', 'the pick screen never lists ...'), async (...) => { ... });
//   test('SMOKE: sign in', smoke(), async (...) => { ... });
// `test/uat/run.sh --tag <name>` runs the tests tagged @<name> (a flow name, or `smoke`).
type Details = { tag: string[]; annotation: { type: string; description: string }[] };

function checkName(name: string): void {
  if (!/^[a-z][a-z0-9-]*$/.test(name) || name === 'smoke') throw new Error(`flow name ${name}: lowercase letters, digits and dashes, not "smoke"`);
}

/** The infrastructure smoke: not a UAT flow. */
export function smoke(): Details {
  return { tag: ['@smoke'], annotation: [{ type: 'uat-kind', description: 'SMOKE (infrastructure, not a UAT flow)' }] };
}

/** A UAT flow test (the screens only, after setup), tagged @<name>. */
export function flow(name: string): Details {
  checkName(name);
  return { tag: [`@${name}`], annotation: [{ type: 'uat-flow', description: name }] };
}

/**
 * A flow test that asserts the correct behaviour of a step that does not work yet. `symptom` says
 * what goes wrong (self-contained, no external ticket needed). Not run unless --known-gaps.
 */
export function knownGap(name: string, symptom: string): Details {
  checkName(name);
  return {
    tag: [`@${name}`, '@known-gap'],
    annotation: [{ type: 'uat-flow', description: name }, { type: 'KNOWN-GAP', description: symptom }],
  };
}
