/**
 * The mobile contract (src/integration-test/resources/mobile-contract/mobile-contract.json: every
 * call openboxes-mobile makes, with the response fields it reads) and its checks:
 * - url(id, vars): the app's URL for an entry; the method and path template come from the JSON,
 *   a test only supplies the {placeholder} values, so it cannot call a URL the app does not call;
 * - check(): the status class the app treats as success and every high/medium response field's
 *   JSON kind (`a[]` = every element; an empty list on the way to a field is a fixture gap).
 */
import * as fs from 'fs';
import * as path from 'path';
import { REPO_ROOT } from './env';

export interface Field { path: string; type: string; confidence: 'high' | 'medium' | 'low'; evidence?: string }
export interface Entry {
  id: string; method: string; path: string;
  query: { name: string; value: string; optional?: boolean }[];
  extraQueryFrom: string | null; axiosConfig: string | null; body: unknown;
  response: { envelope: string; expectedStatus: string; used: boolean; parseResponse?: boolean; fields: Field[] };
}
export interface Contract { mobile: { sha: string }; entryCount: number; entries: Entry[] }

export const CONTRACT_FILE = path.join(REPO_ROOT, 'src', 'integration-test', 'resources', 'mobile-contract', 'mobile-contract.json');

let loaded: { contract: Contract; byId: Map<string, Entry> } | undefined;

/**
 * The contract, read on first use (not at import), so setup, flows and the web UAT run without the
 * file; only what uses the contract (the mobile-api tests, the coverage report) needs it.
 */
export function contract(): Contract {
  if (!loaded) {
    let text: string;
    try {
      text = fs.readFileSync(CONTRACT_FILE, 'utf8');
    } catch (e) {
      throw new Error(`the mobile contract is missing: expected ${CONTRACT_FILE} (test/e2e/README.md "Prerequisites"; ` +
        `only the mobile-api project and its coverage report need it): ${(e as Error).message}`);
    }
    const c = JSON.parse(text) as Contract;
    loaded = { contract: c, byId: new Map(c.entries.map((e) => [e.id, e])) };
  }
  return loaded.contract;
}

export function entry(id: string): Entry {
  contract();
  const e = loaded!.byId.get(id);
  if (!e) throw new Error(`no contract entry ${id}`);
  return e;
}

function enc(v: unknown): string {
  return encodeURIComponent(String(v));
}

export type Vars = Record<string, unknown>;

export function url(id: string, vars: Vars = {}): string {
  const e = entry(id);
  const p = e.path.replace(/\{([^}]+)\}/g, (_all, name: string) => {
    if (!(name in vars)) throw new Error(`${id}: no value for path placeholder {${name}}`);
    return enc(vars[name]);
  });
  const query: string[] = [];
  for (const q of e.query) {
    const m = /^\{([^}]+)\}$/.exec(q.value);
    if (m) {
      const v = vars[m[1]];
      if (v === undefined || v === null) {
        if (q.optional) continue;
        throw new Error(`${id}: no value for query placeholder {${m[1]}}`);
      }
      for (const item of Array.isArray(v) ? v : [v]) query.push(`${q.name}=${enc(item)}`);
    } else if (q.value.includes('{')) {
      throw new Error(`${id}: partial placeholder in ${q.name}=${q.value}`);
    } else {
      query.push(`${q.name}=${q.value}`);
    }
  }
  if (e.extraQueryFrom && vars[e.extraQueryFrom] && typeof vars[e.extraQueryFrom] === 'object') {
    for (const [k, v] of Object.entries(vars[e.extraQueryFrom] as Record<string, unknown>)) query.push(`${k}=${enc(v)}`);
  }
  if (e.axiosConfig) {
    const cfg = vars[e.axiosConfig.slice(1, -1)] as { params?: Record<string, unknown> } | undefined;
    for (const [k, v] of Object.entries(cfg?.params ?? {})) query.push(`${k}=${enc(v)}`);
  }
  return query.length ? `${p}?${query.join('&')}` : p;
}

const ABSENT = Symbol('absent');

export function kind(v: unknown): string {
  if (v === ABSENT) return 'absent';
  if (v === null || v === undefined) return 'null';
  if (Array.isArray(v)) return 'array';
  if (typeof v === 'object') return 'object';
  return typeof v; // string, number, boolean
}

/** The app's src/utils/utils.ts parseResponse: lodash _.set of every key, so "a.b" becomes a: {b}. */
export function expandFlatKeys(v: unknown): unknown {
  if (Array.isArray(v)) return v.map(expandFlatKeys);
  if (v && typeof v === 'object') {
    const out: Record<string, unknown> = {};
    for (const [k, val] of Object.entries(v as Record<string, unknown>)) {
      const parts = k.split('.');
      let cur = out;
      parts.forEach((part, i) => {
        if (i === parts.length - 1) cur[part] = expandFlatKeys(val);
        else {
          if (!cur[part] || typeof cur[part] !== 'object' || Array.isArray(cur[part])) cur[part] = {};
          cur = cur[part] as Record<string, unknown>;
        }
      });
    }
    return out;
  }
  return v;
}

export function tokens(p: string): string[] {
  const out: string[] = [];
  const re = /\.?([A-Za-z_$][A-Za-z0-9_$]*)|\[\]|\[0\]|\["([^"]+)"\]/g;
  let consumed = 0;
  let m: RegExpExecArray | null;
  while ((m = re.exec(p)) !== null) {
    if (m.index !== consumed) throw new Error(`bad field path ${p}`);
    consumed = re.lastIndex;
    if (m[0] === '[]' || m[0] === '[0]') out.push(m[0]);
    else if (m[2] !== undefined) out.push('.' + m[2]);
    else out.push('.' + m[1]);
  }
  if (consumed !== p.length) throw new Error(`bad field path ${p}`);
  return out;
}

export function checkField(body: unknown, p: string, type: string, emptyOk: string[]): string[] {
  const problems = new Set<string>();
  const allowed = new Set(type.split('|'));
  let values: unknown[] = [body];
  let walked = '';
  for (const t of tokens(p)) {
    const next: unknown[] = [];
    for (const v of values) {
      const k = kind(v);
      if (k === 'null' || k === 'absent') continue; // under a null/missing parent: not checked here
      if (t === '[]' || t === '[0]') {
        if (k !== 'array') { problems.add(`${p}: ${walked || '(body)'} is ${k}, the app reads it as an array`); continue; }
        const list = v as unknown[];
        if (list.length === 0 && !emptyOk.some((x) => walked.startsWith(x) || p.startsWith(x))) {
          problems.add(`${p}: ${walked || '(body)'} is an empty array, nothing to check (fixture gap)`);
        }
        next.push(...(t === '[0]' ? list.slice(0, 1) : list));
      } else {
        const key = t.substring(1);
        if (k !== 'object') { problems.add(`${p}: ${walked || '(body)'} is ${k}, the app reads .${key} on it`); continue; }
        const o = v as Record<string, unknown>;
        next.push(Object.prototype.hasOwnProperty.call(o, key) ? o[key] : ABSENT);
      }
    }
    walked += t;
    values = next;
  }
  for (const leaf of values) {
    const k = kind(leaf);
    if (!allowed.has(k)) {
      const shown = ['string', 'number', 'boolean'].includes(k) ? ` (${String(leaf)})` : '';
      problems.add(`${p}: ${k}${shown}, the app expects ${type}`);
    }
  }
  return [...problems];
}

export function check(e: Entry, status: number, body: unknown, emptyOk: string[] = [], except: string[] = []): string[] {
  const r = e.response;
  if (r.expectedStatus === '2xx' && !(status >= 200 && status < 300)) return [`status ${status}, the app expects ${r.expectedStatus}`];
  if (r.envelope === 'none') return [];
  if (body === undefined) return ['response body is not JSON'];
  let b = body;
  if (r.parseResponse && b && typeof b === 'object' && !Array.isArray(b) && 'data' in (b as object)) {
    b = { ...(b as object), data: expandFlatKeys((b as { data: unknown }).data) };
  }
  const problems: string[] = [];
  for (const f of r.fields) {
    if (f.confidence === 'low' || except.some((x) => f.path.startsWith(x))) continue;
    problems.push(...checkField(b, f.path, f.type, emptyOk));
  }
  return problems;
}

/** Field paths of an entry except the ones listed (a typed scan result only carries its type's fields). */
export function allBut(id: string, keep: string[]): string[] {
  return entry(id).response.fields.map((f) => f.path).filter((p) => !keep.includes(p));
}
