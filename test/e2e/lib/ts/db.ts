/**
 * Direct SQL for the fixtures OBAF has no API for (an order already in PICKING with a picklist and a
 * shipment, a SHIPPED inbound on a pallet, a backordered sales order), and for read-back assertions.
 * Rows are written the way GORM writes them (32-hex uuid ids, version 0, audit columns). Through the gateway's loopback port only.
 */
import { createPool, Pool, RowDataPacket } from 'mysql2/promise';
import { randomUUID } from 'crypto';
import { DB_PORT } from './env';

let pool: Pool | undefined;

export function db(): Pool {
  if (!pool) {
    pool = createPool({
      host: '127.0.0.1', port: DB_PORT, user: 'openboxes', password: 'openboxes', database: 'openboxes',
      connectionLimit: 4, timezone: 'Z', dateStrings: true,
    });
  }
  return pool;
}

export async function closeDb(): Promise<void> {
  if (pool) { await pool.end(); pool = undefined; }
}

export function newId(): string {
  return randomUUID().replace(/-/g, '');
}

export async function rows<T = RowDataPacket>(sql: string, params: unknown[] = []): Promise<T[]> {
  const [r] = await db().query<RowDataPacket[]>(sql, params);
  return r as unknown as T[];
}

export async function one<T = RowDataPacket>(sql: string, params: unknown[] = []): Promise<T | undefined> {
  return (await rows<T>(sql, params))[0];
}

export async function scalar<T = unknown>(sql: string, params: unknown[] = []): Promise<T | undefined> {
  const r = await one<Record<string, unknown>>(sql, params);
  return r ? (Object.values(r)[0] as T) : undefined;
}

/** INSERT one row; `id`, `version`, `date_created`, `last_updated` are filled when the table has them and they are not given. */
export async function insert(table: string, values: Record<string, unknown>): Promise<string> {
  const cols = await columns(table);
  const v: Record<string, unknown> = { ...values };
  if (cols.has('id') && v.id === undefined) v.id = newId();
  if (cols.has('version') && v.version === undefined) v.version = 0;
  for (const c of ['date_created', 'last_updated']) if (cols.has(c) && v[c] === undefined) v[c] = new Date();
  for (const c of ['created_by_id', 'updated_by_id']) if (cols.has(c) && v[c] === undefined) v[c] = '1';
  const keys = Object.keys(v);
  for (const k of keys) if (!cols.has(k)) throw new Error(`${table} has no column ${k}`);
  await db().query(`INSERT INTO \`${table}\` (${keys.map((k) => `\`${k}\``).join(',')}) VALUES (${keys.map(() => '?').join(',')})`,
    keys.map((k) => v[k]));
  return v.id as string;
}

const columnCache = new Map<string, Set<string>>();
async function columns(table: string): Promise<Set<string>> {
  let c = columnCache.get(table);
  if (!c) {
    const r = await rows<{ c: string }>('SELECT column_name AS c FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ?', [table]);
    if (!r.length) throw new Error(`no table ${table}`);
    c = new Set(r.map((x) => x.c));
    columnCache.set(table, c);
  }
  return c;
}

/** Polls until `fn` returns a truthy value or the timeout passes; returns the last value. */
export async function eventually<T>(fn: () => Promise<T>, timeoutMs = 30000, everyMs = 300): Promise<T> {
  const deadline = Date.now() + timeoutMs;
  let v = await fn();
  while (!v && Date.now() < deadline) {
    await new Promise((r) => setTimeout(r, everyMs));
    v = await fn();
  }
  return v;
}

/**
 * Reads `sql` until it answers `expected` or `timeoutMs` passes; returns the last value read. OBAF's
 * @Transactional API controllers render the JSON reply before the transaction commits, so a
 * row written by the request can be invisible for a moment after the 200 arrives.
 */
export async function committed<T = unknown>(sql: string, params: unknown[], expected: T, timeoutMs = 3000): Promise<T | undefined> {
  const deadline = Date.now() + timeoutMs;
  let v = await scalar<T>(sql, params);
  while (v !== expected && Date.now() < deadline) {
    await new Promise((r) => setTimeout(r, 200));
    v = await scalar<T>(sql, params);
  }
  return v;
}
