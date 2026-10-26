/**
 * Contract coverage of the mobile-api project: which mobile contract entries (lib/ts/contract.ts) have a
 * test (completeness), which were EXERCISED against the live server (a test with a `covers`
 * annotation that ran, whatever its outcome), which have a PASSING test, and which are only reached
 * by KNOWN-GAP (fixme) tests. Writes JSON and prints one summary line plus any entry without a test.
 */
import type { FullConfig, Reporter, Suite, TestCase, TestResult } from '@playwright/test/reporter';
import * as fs from 'fs';
import * as path from 'path';
import { Contract, contract } from './contract';

type Row = { tests: number; ran: number; passed: number; conformant: number; failed: number; gaps: string[] };

export default class CoverageReporter implements Reporter {
  private rows = new Map<string, Row>();
  private unknown = new Set<string>();
  private active = false;
  private contract?: Contract;
  constructor(private opts: { outputFile: string }) {}

  onBegin(_config: FullConfig, suite: Suite) {
    const tests = suite.allTests();
    // the contract is read only when a test covers an entry (a run without mobile-api never needs it)
    if (!tests.some((t) => this.ids(t).length)) return;
    try {
      this.contract = contract();
    } catch (e) {
      console.log(`[e2e-contract] no coverage report: ${(e as Error).message}`);
      return;
    }
    for (const e of this.contract.entries) this.rows.set(e.id, { tests: 0, ran: 0, passed: 0, conformant: 0, failed: 0, gaps: [] });
    for (const t of tests) {
      for (const id of this.ids(t)) {
        const r = this.rows.get(id);
        if (!r) { this.unknown.add(id); continue; }
        this.active = true;
        r.tests++;
        const gap = t.annotations.find((a) => a.type === 'KNOWN-GAP');
        if (gap && !r.gaps.includes(gap.description!)) r.gaps.push(gap.description!);
      }
    }
  }

  private ids(t: TestCase): string[] {
    return t.annotations.filter((a) => a.type === 'covers').map((a) => a.description!);
  }

  onTestEnd(t: TestCase, result: TestResult) {
    if (result.status === 'skipped') return;
    for (const id of this.ids(t)) {
      const r = this.rows.get(id);
      if (!r) continue;
      r.ran++;
      if (result.status === 'passed') {
        r.passed++;
        if (!t.annotations.some((a) => a.type === 'characterised')) r.conformant++;
      } else r.failed++;
    }
  }

  onEnd() {
    if (!this.active || !this.contract) return;
    const total = this.rows.size;
    const withTest = [...this.rows.values()].filter((r) => r.tests > 0).length;
    const exercised = [...this.rows.values()].filter((r) => r.ran > 0).length;
    const passing = [...this.rows.values()].filter((r) => r.passed > 0).length;
    const conformant = [...this.rows.values()].filter((r) => r.conformant > 0).length;
    const notConformant = [...this.rows.entries()].filter(([, r]) => r.passed > 0 && r.conformant === 0).map(([id]) => id);
    const gapOnly = [...this.rows.entries()].filter(([, r]) => r.tests > 0 && r.ran === 0).map(([id]) => id);
    const pct = (n: number) => Math.round((1000 * n) / total) / 10;
    const out = {
      mobileSha: this.contract.mobile.sha, entries: total,
      withTest, exercised, passing, conformant, percentWithTest: pct(withTest), percentExercised: pct(exercised), percentPassing: pct(passing),
      percentConformant: pct(conformant), passingOnlyAsCharacterised: notConformant,
      noTest: [...this.rows.entries()].filter(([, r]) => r.tests === 0).map(([id]) => id),
      onlyKnownGaps: gapOnly, unknownIds: [...this.unknown],
      perEntry: Object.fromEntries(this.rows),
    };
    fs.mkdirSync(path.dirname(this.opts.outputFile), { recursive: true });
    fs.writeFileSync(this.opts.outputFile, JSON.stringify(out, null, 2) + '\n');
    console.log(`[e2e-contract] ${total} entries: ${withTest} with a test (${out.percentWithTest} %), ` +
      `${exercised} exercised against the server (${out.percentExercised} %), ${passing} passing (${out.percentPassing} %), ` +
      `${conformant} with a passing test of the app's expected answer (${out.percentConformant} %)`);
    if (notConformant.length) console.log(`[e2e-contract] passing only as characterised (the app's expected answer is a KNOWN-GAP): ${notConformant.join(', ')}`);
    if (out.noTest.length) console.log(`[e2e-contract] NO TEST: ${out.noTest.join(', ')}`);
    if (out.unknownIds.length) console.log(`[e2e-contract] covers ids not in the contract: ${out.unknownIds.join(', ')}`);
    if (gapOnly.length) console.log(`[e2e-contract] only KNOWN-GAP tests: ${gapOnly.join(', ')}`);
  }
}
