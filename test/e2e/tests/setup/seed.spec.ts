import * as fs from 'fs';
import * as path from 'path';
import { test, expect } from '../../lib/ts/test';
import { MAIN_WAREHOUSE } from '../../lib/ts/env';
import { STORAGE_STATE } from '../../playwright.config';

test('seed: Test Mobile DC exists (Location API) and the seed SQL was applied', async ({ fx }) => {
  const dc = await fx.mobileDc();
  expect(dc).toBeTruthy();
  expect(await fx.documentId('barcodeLabel:product')).toBeTruthy();
  expect(await fx.eventTypeId('PACKING_SCHEDULED')).toBeTruthy();
});

test('seed: the web app signs admin in to Main Warehouse (saved for the flows)', async ({ page }) => {
  await page.goto('auth/login');
  await page.locator('#username').fill('admin');
  await page.locator('#password').fill('password');
  await page.locator('#loginButton').click();
  // a fresh user has no location yet: the chooser opens; pick Main Warehouse through the app's own route
  await page.waitForLoadState('networkidle');
  const r = await page.request.put(`api/chooseLocation/${MAIN_WAREHOUSE}`);
  expect(r.status()).toBe(200);
  await page.goto('dashboard/index');
  await expect(page.locator('body')).toContainText('Main Warehouse');
  fs.mkdirSync(path.dirname(STORAGE_STATE), { recursive: true });
  await page.context().storageState({ path: STORAGE_STATE });
});
