/**
 * An openboxes-mobile session against the running OBAF, made the way the app makes it
 * (src/utils/ApiClient.ts: axios, baseURL = <server>/openboxes/api, withCredentials): POST /login
 * {username,password}, GET /getAppContext, PUT /chooseLocation/{id}, then the JSESSIONID cookie
 * on every call; axios's Accept header and a JSON body; no X-Requested-With. A 3xx is followed as
 * a GET, as the app's XMLHttpRequest does (recorded as redirectedFrom).
 */
import { APIRequestContext, APIResponse, expect, request as pwRequest } from '@playwright/test';
import { API_URL, ADMIN, isStackUrl } from './env';
import { check, entry, url, Vars } from './contract';

export const AXIOS_ACCEPT = 'application/json, text/plain, */*';

export class CallResult {
  constructor(
    readonly id: string, readonly method: string, readonly url: string, readonly status: number,
    readonly contentType: string, readonly text: string, readonly json: any, readonly redirectedFrom?: string,
  ) {}

  get data(): any { return this.json && typeof this.json === 'object' && !Array.isArray(this.json) ? this.json : undefined; }

  /** The same exchange with `data` cut to this test's own rows (the database is shared and never cleaned). */
  rows(keep: (row: any) => boolean): CallResult {
    const d = this.data;
    if (!d || !Array.isArray(d.data)) return this;
    const kept = d.data.filter(keep);
    expect(kept.length, `${this.id}: none of the test's own rows came back: ${this}`).toBeGreaterThan(0);
    return new CallResult(this.id, this.method, this.url, this.status, this.contentType, this.text, { ...d, data: kept }, this.redirectedFrom);
  }

  problems(emptyOk: string[] = [], except: string[] = []): string[] {
    return check(entry(this.id), this.status, this.json, emptyOk, except);
  }

  /** Fails with every contract problem at once (route, status class, fields and their JSON kinds). */
  holds(emptyOk: string[] = [], except: string[] = []): this {
    const p = this.problems(emptyOk, except);
    expect(p, `${this.id}: ${this.method} ${this.url} -> ${this.status}\n  ${p.join('\n  ')}\n  body: ${this.text.slice(0, 1500)}`).toEqual([]);
    return this;
  }

  toString(): string {
    return `${this.method} ${this.url}${this.redirectedFrom ? ` (followed ${this.redirectedFrom})` : ''} -> ${this.status} ${this.text.slice(0, 300)}`;
  }
}

function parse(text: string, contentType: string): any {
  if (!text) return undefined;
  const t = text.trim();
  if (contentType.includes('json') || t.startsWith('{') || t.startsWith('[')) {
    try { return JSON.parse(t); } catch { return undefined; }
  }
  return undefined;
}

/** Every exchange of the run, for the coverage report (id -> statuses seen). */
export const exchanges: { id: string; method: string; url: string; status: number }[] = [];

export class MobileApp {
  private constructor(readonly ctx: APIRequestContext, readonly locationId: string | null) {}

  /** A fresh cookie jar, not signed in. */
  static async anonymous(): Promise<MobileApp> {
    return new MobileApp(await newContext(), null);
  }

  /** The app's sign-in; each step must answer 2xx. */
  static async signIn(locationId: string, user = ADMIN.username, password = ADMIN.password): Promise<MobileApp> {
    const app = new MobileApp(await newContext(), locationId);
    const login = await app.raw('POST', '/login', { username: user, password });
    expect(login.status(), `POST /login -> ${login.status()} ${await login.text()}`).toBe(200);
    const ctx = await app.raw('GET', '/getAppContext');
    expect(ctx.status(), 'GET /getAppContext').toBe(200);
    const choose = await app.raw('PUT', `/chooseLocation/${locationId}`);
    expect(choose.status(), `PUT /chooseLocation -> ${choose.status()}`).toBe(200);
    return app;
  }

  async dispose(): Promise<void> { await this.ctx.dispose(); }

  async raw(method: string, path: string, body?: unknown): Promise<APIResponse> {
    const target = path.startsWith('http') ? path : API_URL + path;
    if (!isStackUrl(target)) throw new Error(`refusing a request outside the stack (only the app and label stub ports on loopback): ${target}`);
    const headers: Record<string, string> = { Accept: AXIOS_ACCEPT };
    const opts: Parameters<APIRequestContext['fetch']>[1] = { method, headers, maxRedirects: 0, failOnStatusCode: false };
    if (body !== undefined && body !== null) {
      headers['Content-Type'] = 'application/json';
      opts!.data = typeof body === 'string' ? body : JSON.stringify(body);
    }
    return this.ctx.fetch(target, opts);
  }

  /** One contract call, the app's way. */
  async call(id: string, vars: Vars = {}, body?: unknown): Promise<CallResult> {
    const e = entry(id);
    const u = url(id, vars);
    let b = body;
    const eb = e.body as Record<string, unknown> | null;
    if (b === undefined && ['POST', 'PUT', 'PATCH'].includes(e.method) && eb && typeof eb === 'object' && Object.keys(eb).length === 0) b = {};
    let r = await this.raw(e.method, u, b);
    let redirectedFrom: string | undefined;
    const location = r.headers()['location'];
    if ([301, 302, 303].includes(r.status()) && location) {
      redirectedFrom = `${r.status()} from ${e.method} ${u}`;
      r = await this.raw('GET', location);
    }
    const text = await r.text();
    const ct = r.headers()['content-type'] ?? '';
    exchanges.push({ id, method: e.method, url: u, status: r.status() });
    return new CallResult(id, e.method, u, r.status(), ct, text, parse(text, ct), redirectedFrom);
  }
}

async function newContext(): Promise<APIRequestContext> {
  return pwRequest.newContext({ extraHTTPHeaders: { Accept: AXIOS_ACCEPT } });
}
