/**
 * The audit checkpoints' database access: READ ONLY, three ways over.
 *   1. it signs in as `uat_audit`, a database user with SELECT only (test/uat/run.sh creates it);
 *   2. its session is `SET SESSION TRANSACTION READ ONLY`;
 *   3. every statement passes test/uat/lib/sql-guard.js (one SELECT / WITH / SHOW / EXPLAIN, no word that
 *      writes or locks).
 * Audits compare what the screen claimed with the database. They never drive the flow: they get these
 * read helpers and nothing else (no page, no API session).
 */
import { createConnection, Connection, RowDataPacket } from 'mysql2/promise';
import { DB_PORT } from '../../../e2e/lib/ts/env';
// eslint-disable-next-line @typescript-eslint/no-var-requires
const { assertReadOnly } = require('../../lib/sql-guard.js') as { assertReadOnly: (sql: string) => string };

export const AUDIT_USER = process.env.UAT_AUDIT_USER || 'uat_audit';
const AUDIT_PASSWORD = process.env.UAT_AUDIT_PASSWORD || 'uat_audit';

let conn: Promise<Connection> | undefined;

async function connection(): Promise<Connection> {
  if (!conn) {
    conn = (async () => {
      let c: Connection;
      try {
        c = await createConnection({
          host: '127.0.0.1', port: DB_PORT, user: AUDIT_USER, password: AUDIT_PASSWORD, database: 'openboxes',
          timezone: 'Z', dateStrings: true, multipleStatements: false,
        });
      } catch (e) {
        conn = undefined;
        throw new Error(`audit: cannot connect as ${AUDIT_USER} on 127.0.0.1:${DB_PORT} (test/uat/run.sh creates this read-only user): ${(e as Error).message}`);
      }
      await c.query('SET SESSION TRANSACTION READ ONLY');
      return c;
    })();
  }
  return conn;
}

export async function closeAuditDb(): Promise<void> {
  if (conn) {
    const c = conn;
    conn = undefined;
    try { await (await c).end(); } catch { /* already gone */ }
  }
}

/** Read-only queries for audits and declared start states. */
export interface ReadOnly {
  rows<T = RowDataPacket>(sql: string, params?: unknown[]): Promise<T[]>;
  one<T = RowDataPacket>(sql: string, params?: unknown[]): Promise<T | undefined>;
  scalar<T = unknown>(sql: string, params?: unknown[]): Promise<T | undefined>;
}

export const readOnly: ReadOnly = {
  async rows<T>(sql: string, params: unknown[] = []) {
    assertReadOnly(sql);
    const [r] = await (await connection()).query<RowDataPacket[]>(sql, params);
    return r as unknown as T[];
  },
  async one<T>(sql: string, params: unknown[] = []) {
    return (await readOnly.rows<T>(sql, params))[0];
  },
  async scalar<T>(sql: string, params: unknown[] = []) {
    const r = await readOnly.one<Record<string, unknown>>(sql, params);
    return r ? (Object.values(r)[0] as T) : undefined;
  },
};

/** Readers the flows' audits and start states share (all through `readOnly`). */
export const reads = {
  /** On hand of a product at a facility (product_availability, refreshed asynchronously by OBAF). */
  async onHand(facilityId: string, productId: string): Promise<number> {
    return Number(await readOnly.scalar('SELECT COALESCE(SUM(quantity_on_hand), 0) FROM product_availability WHERE location_id = ? AND product_id = ?', [facilityId, productId]));
  },
  /** On hand per bin location number (bins with a non-zero quantity only). */
  async onHandByBin(facilityId: string, productId: string): Promise<Record<string, number>> {
    const r = await readOnly.rows<{ bin: string | null; q: number }>(
      `SELECT bin.location_number AS bin, SUM(pa.quantity_on_hand) AS q FROM product_availability pa
         LEFT JOIN location bin ON bin.id = pa.bin_location_id
        WHERE pa.location_id = ? AND pa.product_id = ? GROUP BY bin.location_number HAVING SUM(pa.quantity_on_hand) <> 0`, [facilityId, productId]);
    return Object.fromEntries(r.map((x) => [x.bin ?? '(no bin)', Number(x.q)]));
  },
  /** Transaction entries of a product at a facility (count). */
  async transactionEntries(facilityId: string, productId: string): Promise<number> {
    return Number(await readOnly.scalar(
      `SELECT COUNT(*) FROM transaction_entry te JOIN \`transaction\` t ON t.id = te.transaction_id
         JOIN inventory_item ii ON ii.id = te.inventory_item_id JOIN location l ON l.inventory_id = t.inventory_id
        WHERE l.id = ? AND ii.product_id = ?`, [facilityId, productId]));
  },
  async locationName(id: string): Promise<string | undefined> {
    return readOnly.scalar<string>('SELECT name FROM location WHERE id = ?', [id]);
  },
  /** A user's username, active flag and role types (undefined when there is no such user). */
  async user(username: string): Promise<{ active: boolean; roles: string[] } | undefined> {
    const u = await readOnly.one<{ id: string; active: number | Buffer }>(
      'SELECT u.id, p.active + 0 AS active FROM `user` u JOIN person p ON p.id = u.id WHERE u.username = ?', [username]);
    if (!u) return undefined;
    const roles = await readOnly.rows<{ r: string }>(
      'SELECT ro.role_type AS r FROM user_role ur JOIN role ro ON ro.id = ur.role_id WHERE ur.user_id = ? ORDER BY ro.role_type', [u.id]);
    return { active: Number(u.active) === 1, roles: roles.map((x) => x.r) };
  },
  async requisitionStatus(id: string): Promise<string | undefined> {
    return readOnly.scalar<string>('SELECT status FROM requisition WHERE id = ?', [id]);
  },
  async shipmentStatus(id: string): Promise<string | undefined> {
    return readOnly.scalar<string>('SELECT current_status FROM shipment WHERE id = ?', [id]);
  },
  async orderStatus(id: string): Promise<string | undefined> {
    return readOnly.scalar<string>('SELECT status FROM `order` WHERE id = ?', [id]);
  },
};
