/**
 * Per-flow test data with unique names, for SETUP only (before the first screen step). Built on the
 * e2e suite's fixtures (test/e2e/lib/ts/fixtures.ts: OpenBoxes' own APIs for locations, products and
 * stock; GORM-shaped SQL for what no API creates). Every name carries the flow and a unique suffix
 * (`URECEIVING-STOR-<uid>`), so each flow runs alone or in any order on the shared database.
 *
 * After setup, a flow drives only the screens: nothing here may be called once the first `step` ran
 * (the `data` fixture enforces it: a factory call after the first step fails the test).
 */
import { createHash } from 'crypto';
import { Fixtures } from '../../../e2e/lib/ts/fixtures';
import { insert, rows, scalar } from '../../../e2e/lib/ts/db';
import { uid } from '../../../e2e/lib/ts/env';

/** OBAF's PasswordCodec: SHA-1, Base64. */
export function obafPassword(plain: string): string {
  return createHash('sha1').update(plain, 'utf8').digest('base64');
}

export interface FlowUser { id: string; username: string; password: string; firstName: string; lastName: string; roles: string[] }

export class FlowData {
  private fxPromise?: Promise<Fixtures>;
  private sealed = false;
  readonly created: { kind: string; name: string; id: string }[] = [];

  constructor(readonly flow: string) {}

  /** `U<flow>-<label>`: the prefix every name of this flow carries (a unique suffix is added). */
  prefix(label: string): string { return `U${this.flow}-${label}`.toUpperCase(); }
  unique(label: string): string { return `${this.prefix(label)}-${uid()}`; }

  /** Called by the fixture when the first screen step starts: setup is over. */
  seal(): void { this.sealed = true; }

  private async fx(): Promise<Fixtures> {
    if (this.sealed) throw new Error('test data is created before the first step only (after it, the flow drives the screens)');
    if (!this.fxPromise) this.fxPromise = Fixtures.create();
    return this.fxPromise;
  }

  async dispose(): Promise<void> {
    if (this.fxPromise) await (await this.fxPromise).dispose();
  }

  /** A depot (Location API) with the depot activities plus `extra`. */
  async depot(label: string, extra: string[] = []): Promise<{ id: string; name: string }> {
    const name = this.unique(label);
    const id = await (await this.fx()).depot('', extra, { number: name, name });
    this.created.push({ kind: 'depot', name, id });
    return { id, name };
  }

  /** A bin under a depot; `activities` replace the bin type's (PICK_STOCK / PUTAWAY_STOCK to hold stock). */
  async bin(depotId: string, label: string, activities?: string[]): Promise<{ id: string; number: string }> {
    const b = await (await this.fx()).bin(depotId, this.prefix(label), activities);
    this.created.push({ kind: 'bin', name: b.number, id: b.id });
    return b;
  }

  /** A product with a unique code, name and UPC (Product API). */
  async product(label: string): Promise<{ id: string; productCode: string; upc: string; name: string }> {
    const p = await (await this.fx()).product(this.prefix(label));
    this.created.push({ kind: 'product', name: `${p.productCode} ${p.name}`, id: p.id });
    return p;
  }

  /** Record stock (the Record Stock page's call): rows are [binId, quantity, lotNumber?, expiry?]. */
  async stock(facilityId: string, productId: string, lines: [string, number, string?, Date?][]): Promise<void> {
    await (await this.fx()).stock(facilityId, productId, lines);
  }

  /**
   * A user with a unique username, OBAF's password hash and the given global roles (role types,
   * default ROLE_MANAGER). Through SQL: OBAF has no user API (GORM-shaped person + user rows).
   */
  async user(label: string, opts: { roles?: string[]; password?: string } = {}): Promise<FlowUser> {
    await this.fx();
    const username = this.unique(label).toLowerCase();
    const password = opts.password ?? `Uat-${uid()}`;
    const roles = opts.roles ?? ['ROLE_MANAGER'];
    const firstName = 'Uat';
    const lastName = username;
    const id = await insert('person', { first_name: firstName, last_name: lastName, email: `${username}@uat.invalid`, active: 1 });
    await insert('user', { id, username, password: obafPassword(password), locale: 'en', remember_last_location: 0 });
    for (const r of roles) {
      const roleId = await scalar<string>('SELECT id FROM role WHERE role_type = ?', [r]);
      if (!roleId) throw new Error(`no role ${r}`);
      await rows('INSERT INTO user_role (user_id, role_id) VALUES (?, ?)', [id, roleId]);
    }
    this.created.push({ kind: 'user', name: username, id });
    return { id, username, password, firstName, lastName, roles };
  }
}
