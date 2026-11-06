import * as path from 'path';

/** test/e2e (this file is test/e2e/lib/ts/env.ts). */
export const E2E_DIR = path.resolve(__dirname, '..', '..');
/** The repository root (the OBAF checkout under test). */
export const REPO_ROOT = path.resolve(E2E_DIR, '..', '..');
/** Work files and reports (build/ is ignored by git). */
export const WORK_DIR = path.join(REPO_ROOT, 'build', 'e2e');
export const HTTP_PORT = Number(process.env.E2E_HTTP_PORT || 18080);
export const DB_PORT = Number(process.env.E2E_DB_PORT || 13306);
export const LABEL_PORT = Number(process.env.E2E_LABEL_PORT || 18081);
/** The stack profile (lib/common.sh): `default`, or `scenarios` (event delivery to the webhook sink). */
export const PROFILE = process.env.E2E_PROFILE || 'default';
/** The webhook sink's loopback port; the sink runs on the `scenarios` profile only. */
export const SINK_PORT = Number(process.env.E2E_SINK_PORT || 18082);
/** The app's context root; the mobile app's apiBase is `${APP_URL}/api`. */
export const APP_URL = `http://127.0.0.1:${HTTP_PORT}/openboxes`;
export const API_URL = `${APP_URL}/api`;
export const LABEL_STUB_URL = `http://127.0.0.1:${LABEL_PORT}`;
/** The webhook sink: GET /events?since=<n>, DELETE /events (docker/webhooksink.py). */
export const SINK_URL = `http://127.0.0.1:${SINK_PORT}`;
export const ADMIN = { username: 'admin', password: 'password' };
export const MAIN_WAREHOUSE = '1';
export const MAIN_SUPPLIER = '2';
/** Seeded bin LocationType (BIN_LOCATION; supports PUTAWAY_STOCK, PICK_STOCK). */
export const BIN_TYPE = 'cab2b4f35ba2d867015ba2e17e390001';
export const DEPOT_TYPE = '2';

/** The loopback host names the suite talks to (the browser's host resolver maps every other name to NOTFOUND). */
const STACK_HOSTS = ['127.0.0.1', 'localhost'];
/** The loopback ports the suite may reach: the app (through the gateway), the label stub and, on the `scenarios` profile, the webhook sink. */
export const STACK_PORTS = PROFILE === 'scenarios' ? [HTTP_PORT, LABEL_PORT, SINK_PORT] : [HTTP_PORT, LABEL_PORT];

function port(u: URL): number {
  if (u.port) return Number(u.port);
  return u.protocol === 'https:' || u.protocol === 'wss:' ? 443 : 80;
}

/** A loopback URL (any port): 127.0.0.1, localhost or ::1. */
export function isLoopbackHost(url: string): boolean {
  try {
    return [...STACK_HOSTS, '[::1]', '::1'].includes(new URL(url).hostname);
  } catch {
    return false;
  }
}

/**
 * Nothing the suite sends may leave the machine, nor reach another local service: only the
 * STACK_PORTS on 127.0.0.1/localhost (and data:, blob:, about: URLs) pass.
 */
export function isStackUrl(url: string): boolean {
  try {
    const u = new URL(url);
    if (u.protocol === 'data:' || u.protocol === 'blob:' || u.protocol === 'about:') return true;
    if (!['http:', 'https:', 'ws:', 'wss:'].includes(u.protocol)) return false;
    return STACK_HOSTS.includes(u.hostname) && STACK_PORTS.includes(port(u));
  } catch {
    return url.startsWith('data:') || url.startsWith('blob:') || url === 'about:blank';
  }
}

export function uid(): string {
  return (Date.now().toString(36) + Math.floor(Math.random() * 36 ** 4).toString(36).padStart(4, '0')).toUpperCase();
}
