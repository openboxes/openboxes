/**
 * The sealed browser's network policy, shared by e2e (lib/ts/test.ts) and the web UAT
 * (test/uat/web/lib/uat.ts). test/e2e/README.md "CDN mirror".
 *
 *   the stack                     continues: the app's and the label stub's ports on 127.0.0.1/localhost
 *   any other loopback port        aborted and recorded as BLOCKED (no other local service is reachable)
 *   a URL in the mirror            fulfilled from test/e2e/cdn-mirror/cache, byte-exact, with its content type and CORS
 *                                  headers (SRI and crossorigin="anonymous" behave as in production)
 *   a URL the upstream no longer   fulfilled with that status (404), as production browsers get it
 *   serves (manifest `failed`)
 *   an excluded service            aborted: config-gated analytics/captcha/widgets (cdn-mirror/exclude.json)
 *   anything else                  aborted and recorded as MISSING from the mirror (reported, not failed)
 *
 * Nothing reaches the internet: the mirror is files on disk. Below this routing, the browser's host
 * resolver maps every name but 127.0.0.1 and localhost to NOTFOUND (the playwright configs'
 * --host-resolver-rules), so a request the routing does not see (a WebSocket, a DNS prefetch) cannot
 * resolve a remote host either. Each test records, per page, what was
 * served and what was missing (`MirrorLog`), and attaches it as `cdn-mirror` for the report
 * (lib/ts/cdn-mirror-report.ts).
 */
import * as fs from 'fs';
import * as path from 'path';
import type { Route, TestInfo } from '@playwright/test';
import { E2E_DIR, isLoopbackHost, isStackUrl } from './env';

export const MIRROR_DIR = path.join(E2E_DIR, 'cdn-mirror', 'cache');

type FileEntry = { url: string; file: string; contentType: string; size: number };
type Manifest = {
  obafSha: string;
  files: FileEntry[];
  failed: { url: string; status?: number | null }[];
  excluded: { url: string; reason: string }[];
};
type Index = { files: Map<string, FileEntry>; gone: Map<string, number>; exclude: { match: string; reason: string }[]; hosts: Set<string> };

/** host + path + query, scheme-agnostic (a protocol-relative URL on an http page is requested over http). */
export function mirrorKey(url: string): string {
  const u = new URL(url.startsWith('//') ? `https:${url}` : url);
  return `${u.host}${u.pathname}${u.search}`;
}

let index: Index | undefined;
function load(): Index {
  if (index) return index;
  const files = new Map<string, FileEntry>();
  const gone = new Map<string, number>();
  const hosts = new Set<string>();
  let exclude: { match: string; reason: string }[] = [];
  try {
    const m = JSON.parse(fs.readFileSync(path.join(MIRROR_DIR, 'manifest.json'), 'utf8')) as Manifest;
    for (const f of m.files) { files.set(mirrorKey(f.url), f); hosts.add(new URL(f.url).host); }
    for (const f of m.failed ?? []) if (f.status && f.status >= 400 && f.status < 500) gone.set(mirrorKey(f.url), f.status);
  } catch (e) {
    process.stderr.write(`[cdn-mirror] no usable test/e2e/cdn-mirror/cache/manifest.json (${(e as Error).message}): every third-party request is aborted; run test/e2e/cdn-mirror/fetch.sh\n`);
  }
  try {
    exclude = JSON.parse(fs.readFileSync(path.join(E2E_DIR, 'cdn-mirror', 'exclude.json'), 'utf8')).exclude;
  } catch { /* no exclusions */ }
  index = { files, gone, exclude, hosts };
  return index;
}

const bodies = new Map<string, Buffer>();
function body(f: FileEntry): Buffer {
  let b = bodies.get(f.file);
  if (!b) {
    const root = path.resolve(MIRROR_DIR);
    const file = path.resolve(root, f.file);
    if (!file.startsWith(root + path.sep)) throw new Error(`cdn-mirror: manifest path ${f.file} leaves ${root}`);
    b = fs.readFileSync(file);
    bodies.set(f.file, b);
  }
  return b;
}

/** `/openboxes/stockMovement/show/<32 hex>?x` -> `stockMovement/show/{id}`. */
export function pagePath(url: string): string {
  try {
    const u = new URL(url);
    if (u.protocol === 'about:') return url;
    return (u.pathname.replace(/^\/openboxes\/?/, '').replace(/\/[0-9a-f]{32}(?=\/|$)/gi, '/{id}').replace(/\/\d+(?=\/|$)/g, '/{n}') || '/');
  } catch {
    return url;
  }
}

export type MirrorKind = 'served' | 'gone' | 'excluded' | 'missing' | 'blocked';
/** Per page: what the sealed browser did with each third-party URL. */
export class MirrorLog {
  readonly pages = new Map<string, Record<MirrorKind, Set<string>>>();
  add(page: string, kind: MirrorKind, url: string): void {
    let p = this.pages.get(page);
    if (!p) { p = { served: new Set(), gone: new Set(), excluded: new Set(), missing: new Set(), blocked: new Set() }; this.pages.set(page, p); }
    p[kind].add(url);
  }
  /** URLs that were aborted (missing, excluded or blocked). */
  aborted(): string[] {
    return [...this.pages.values()].flatMap((p) => [...p.missing, ...p.excluded, ...p.blocked]);
  }
  toJSON(): Record<string, Record<MirrorKind, string[]>> {
    const out: Record<string, Record<MirrorKind, string[]>> = {};
    for (const k of [...this.pages.keys()].sort()) {
      const p = this.pages.get(k)!;
      out[k] = { served: [...p.served].sort(), gone: [...p.gone].sort(), excluded: [...p.excluded].sort(), missing: [...p.missing].sort(), blocked: [...p.blocked].sort() };
    }
    return out;
  }
  /** Attach to the test (read by the report) and annotate each missing URL (visible in the HTML report). */
  async attach(info: TestInfo): Promise<void> {
    if (!this.pages.size) return;
    await info.attach('cdn-mirror', { body: JSON.stringify(this.toJSON(), null, 2), contentType: 'application/json' });
    for (const [page, p] of this.pages) for (const u of p.missing) info.annotations.push({ type: 'cdn-missing', description: `${page} -> ${u}` });
  }
}

const CORS = {
  'access-control-allow-origin': '*',
  'access-control-allow-methods': 'GET, HEAD, OPTIONS',
  'access-control-allow-headers': '*',
  'timing-allow-origin': '*',
  'cache-control': 'public, max-age=31536000, immutable',
};

/**
 * Handle one routed request under the sealed policy. Returns after the route is continued,
 * fulfilled or aborted.
 */
export async function routeSealed(route: Route, log: MirrorLog): Promise<void> {
  const req = route.request();
  const u = req.url();
  if (isStackUrl(u)) return route.continue();
  let page = '(unknown page)';
  try { page = pagePath(req.frame().url()); } catch { /* a request with no frame (service worker) */ }
  if (isLoopbackHost(u)) {
    log.add(page, 'blocked', u);
    return route.abort('blockedbyclient');
  }
  const idx = load();
  let key = u;
  try { key = mirrorKey(u); } catch { /* not a URL: aborted below */ }
  const f = idx.files.get(key);
  if (f) {
    log.add(page, 'served', u);
    if (req.method() === 'OPTIONS') return route.fulfill({ status: 204, headers: CORS });
    return route.fulfill({ status: 200, headers: { ...CORS, 'content-type': f.contentType }, body: body(f) });
  }
  const gone = idx.gone.get(key);
  if (gone) {
    log.add(page, 'gone', u);
    return route.fulfill({ status: gone, headers: { ...CORS, 'content-type': 'text/plain' }, body: 'Not Found (the upstream no longer serves this URL; test/e2e/cdn-mirror)' });
  }
  const https = u.replace(/^http:/, 'https:');
  log.add(page, idx.exclude.some((x) => https.includes(x.match)) ? 'excluded' : 'missing', u);
  return route.abort('blockedbyclient');
}
