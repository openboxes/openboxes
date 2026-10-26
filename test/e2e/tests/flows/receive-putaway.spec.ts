/**
 * Flow 1: receive an inbound shipment and put it away, in the web app.
 *   API (the Inbound wizard's calls): Main Supplier -> a fresh depot, one lot of 10, sent (SHIPPED).
 *   Browser: Partial Receiving page (receiving now 10 into the shipment's receiving bin, Next,
 *   Receive shipment) -> the stock movement shows Received; Create Putaway page (select the receiving
 *   bin's line, Start Putaway, choose the storage bin, Next, Complete Putaway) -> the putaway order
 *   is COMPLETED; the product's stock is 10 in the storage bin and 0 in receiving.
 */
import { expect, test } from '../../lib/ts/test';
import { Admin, days } from '../../lib/ts/fixtures';
import { MAIN_SUPPLIER, uid } from '../../lib/ts/env';
import { sendInbound } from '../../lib/ts/workflows';
import { chooseLocation, stockMovementStatus } from '../../lib/ts/web';
import { scalar, committed } from '../../lib/ts/db';

test('receive an inbound shipment and put it away (browser)', async ({ page, fx }) => {
  const dc = await fx.depot('RP-DC', ['PARTIAL_RECEIVING']);
  const storage = await fx.bin(dc, 'RP-STOR', ['PICK_STOCK', 'PUTAWAY_STOCK']);
  const p = await fx.product('RP');
  const lotNumber = `RP-${uid()}`;
  const admin = await Admin.at(dc);
  const inbound = await sendInbound(admin, MAIN_SUPPLIER, dc, [{ productId: p.id, quantity: 10, lotNumber, expiry: days(300) }]);
  await admin.dispose();
  await expect.poll(() => fx.shipmentStatus(inbound.shipmentId), { timeout: 3000 }).toBe('SHIPPED');
  await chooseLocation(page, dc);

  await test.step('receive in the Partial Receiving page', async () => {
    await page.goto(`partialReceiving/create/${inbound.shipmentId}`);
    const row = page.getByRole('row').filter({ hasText: lotNumber });
    await expect(row).toBeVisible();
    await expect(row.getByRole('cell', { name: 'Bin Location' })).toContainText(`R-${inbound.identifier}`);
    await row.getByRole('cell', { name: 'Receiving now' }).getByRole('textbox').fill('10');
    await page.getByRole('button', { name: 'Next' }).click();
    await expect(page.getByRole('button', { name: 'Receive shipment' })).toBeVisible();
    await expect(page.getByRole('row').filter({ hasText: lotNumber }).getByRole('cell', { name: 'Receiving now' })).toHaveText('10');
    await page.getByRole('button', { name: 'Receive shipment' }).click();
    await page.waitForURL(/stockMovement\/show\//);
    expect(await stockMovementStatus(page, inbound.stockMovementId)).toContain('Received');
    await expect.poll(() => fx.shipmentStatus(inbound.shipmentId), { timeout: 3000 }).toBe('RECEIVED');
    await fx.awaitOnHand(dc, p.id, 10);
  });

  await test.step('put away in the Create Putaway page', async () => {
    await page.goto('putaway/create');
    const group = page.getByRole('button', { name: new RegExp(`R-${inbound.identifier}`) });
    await expect(group).toBeVisible();
    await group.getByRole('checkbox').click();
    await page.getByTestId('start-putaway').first().click();
    await page.waitForURL(/putaway\/create\/[0-9a-f]+/);
    const putawayId = page.url().split('/').pop()!;
    const line = page.getByRole('row').filter({ hasText: lotNumber });
    await line.getByRole('textbox').last().pressSequentially(storage.number.slice(0, 10));
    await page.locator('.react-select__option').filter({ hasText: storage.number }).first().click();
    await page.getByRole('button', { name: 'Next' }).last().click();
    await expect(page.getByRole('row').filter({ hasText: lotNumber })).toContainText(storage.number);
    await page.getByTestId('complete-putaway-button').last().click();
    await page.waitForURL(/order\/show\//);
    await expect(page.getByRole('row', { name: /Order Type/ })).toContainText('Putaway Order');
    expect(await committed('SELECT status FROM `order` WHERE id = ?', [putawayId], 'COMPLETED')).toBe('COMPLETED');
  });

  await expect.poll(() => fx.onHandByBin(dc, p.id), { timeout: 30000 }).toEqual({ [storage.number]: 10 });
});
