/**
 * The mobile-api project covers the whole contract: every
 * entry of the mobile contract (src/integration-test/resources/mobile-contract/mobile-contract.json) is
 * named by at least one test in this directory, every id named exists, and the contract records the
 * openboxes-mobile commit it was derived from. No server needed.
 */
import * as fs from 'fs';
import * as path from 'path';
import { expect, test } from '@playwright/test';
import { contract } from '../../lib/ts/contract';

const specs = fs.readdirSync(__dirname).filter((f) => f.endsWith('.spec.ts') && f !== path.basename(__filename))
  .map((f) => fs.readFileSync(path.join(__dirname, f), 'utf8')).join('\n');
const named = new Set([...specs.matchAll(/'(src\/[A-Za-z0-9_/.-]+\.ts#[A-Za-z0-9_]+)'/g)].map((m) => m[1]));

test.describe('contract completeness', () => {
  test('every contract entry has a test', () => {
    const missing = contract().entries.map((e) => e.id).filter((id) => !named.has(id));
    expect(missing, 'contract entries with no test').toEqual([]);
    expect(contract().entries.length).toBe(contract().entryCount);
  });

  test('every id the tests name is in the contract', () => {
    const ids = new Set(contract().entries.map((e) => e.id));
    expect([...named].filter((id) => !ids.has(id))).toEqual([]);
  });

  test('the contract records the openboxes-mobile commit it was derived from', () => {
    expect(contract().mobile.sha).toMatch(/^[0-9a-f]{40}$/);
  });
});
