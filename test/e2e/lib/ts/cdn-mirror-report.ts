/**
 * The "served from mirror / missing" report of the sealed browser (lib/ts/cdn-mirror.ts), per page.
 * A Playwright reporter for e2e and the web UAT (writes <outputFile>, a JSON, and the same name with
 * .txt). Missing URLs are reported, never failed (E2E_STRICT_EGRESS=1 fails the e2e test that made one
 * instead).
 */
import * as fs from 'fs';
import * as path from 'path';
import type { Reporter, TestCase, TestResult } from '@playwright/test/reporter';

type Kind = 'served' | 'gone' | 'excluded' | 'missing' | 'blocked';
const KINDS: Kind[] = ['served', 'gone', 'excluded', 'missing', 'blocked'];
type PerPage = Record<string, Record<Kind, Set<string>>>;

export class MirrorAggregate {
  private readonly pages: PerPage = {};
  addResult(result: TestResult): void {
    for (const a of result.attachments) {
      if (a.name !== 'cdn-mirror' || !a.body) continue;
      let data: Record<string, Record<Kind, string[]>>;
      try { data = JSON.parse(a.body.toString('utf8')); } catch { continue; }
      for (const [page, kinds] of Object.entries(data)) {
        const p = (this.pages[page] ??= { served: new Set(), gone: new Set(), excluded: new Set(), missing: new Set(), blocked: new Set() });
        for (const k of KINDS) for (const u of kinds[k] ?? []) p[k].add(u);
      }
    }
  }
  json(): object {
    const pages: Record<string, Record<Kind, string[]>> = {};
    for (const page of Object.keys(this.pages).sort()) {
      const p = this.pages[page];
      pages[page] = { served: [...p.served].sort(), gone: [...p.gone].sort(), excluded: [...p.excluded].sort(), missing: [...p.missing].sort(), blocked: [...p.blocked].sort() };
    }
    const all = (k: Kind) => [...new Set(Object.values(this.pages).flatMap((p) => [...p[k]]))].sort();
    return { totals: { pages: Object.keys(pages).length, served: all('served').length, gone: all('gone'), excluded: all('excluded'), missing: all('missing'), blocked: all('blocked') }, pages };
  }
  lines(): string[] {
    const pages = Object.keys(this.pages).sort();
    if (!pages.length) return ['third-party requests: none (no page loaded a CDN asset)'];
    const all = (k: Kind) => [...new Set(pages.flatMap((p) => [...this.pages[p][k]]))].sort();
    const missing = all('missing');
    const out = [
      `third-party requests (sealed, served from the CDN mirror): ${pages.length} pages, ${all('served').length} URLs served from the mirror, ` +
        `${missing.length} missing from the mirror${all('gone').length ? `, ${all('gone').length} gone upstream (404 as in production)` : ''}` +
        `${all('excluded').length ? `, ${all('excluded').length} excluded services (aborted)` : ''}` +
        `${all('blocked').length ? `, ${all('blocked').length} other loopback ports (blocked)` : ''}`,
    ];
    for (const page of pages) {
      const p = this.pages[page];
      const extra = [
        p.missing.size ? `MISSING ${[...p.missing].sort().join(' ')}` : '',
        p.gone.size ? `gone ${[...p.gone].sort().join(' ')}` : '',
        p.excluded.size ? `excluded ${[...p.excluded].sort().join(' ')}` : '',
        p.blocked.size ? `BLOCKED ${[...p.blocked].sort().join(' ')}` : '',
      ].filter(Boolean).join('; ');
      out.push(`  ${page}: ${p.served.size} served, ${p.missing.size} missing${extra ? `  (${extra})` : ''}`);
    }
    if (missing.length) out.push(`MISSING FROM THE MIRROR (run test/e2e/cdn-mirror/fetch.sh --refresh, or explain): ${missing.join(' ')}`);
    return out;
  }
}

export default class CdnMirrorReporter implements Reporter {
  private readonly agg = new MirrorAggregate();
  private readonly outputFile: string;
  constructor(opts: { outputFile?: string } = {}) {
    this.outputFile = opts.outputFile ?? path.join(process.cwd(), 'cdn-mirror.json');
  }
  onTestEnd(_test: TestCase, result: TestResult): void { this.agg.addResult(result); }
  onEnd(): void {
    fs.mkdirSync(path.dirname(this.outputFile), { recursive: true });
    fs.writeFileSync(this.outputFile, JSON.stringify(this.agg.json(), null, 2) + '\n');
    fs.writeFileSync(this.outputFile.replace(/\.json$/, '.txt'), this.agg.lines().join('\n') + '\n');
  }
  printsToStdio(): boolean { return false; }
}
