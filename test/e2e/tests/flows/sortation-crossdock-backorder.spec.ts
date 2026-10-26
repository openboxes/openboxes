/**
 * Flows 3 and 4: inbound sortation and cross-dock, backorder creation and fulfilment, end to end on
 * the running server with its real event-driven jobs (no job is fired by hand).
 *
 * Site: a depot with AUTOMATED_PUTAWAY_CREATION + AUTO_RECEIVING, an INBOUND_SORTATION bin, a storage
 * bin that is the product's preferred bin, a CROSS_DOCKING lane for LOCAL_DELIVERY.
 *  1. Backorder created: a LOCAL_DELIVERY sales order whose line is backordered (quantityBackordered,
 *     reason BACKORDER). OBAF has no API or screen that writes a backorder (it arrives from the
 *     upstream order system), so it is written in SQL, as that system leaves it.
 *  2. Inbound for it: the Inbound wizard's calls, 10 units, the shipment line carrying the sales order
 *     number as backorder reference (set in SQL: no API binds it either), sent.
 *  3. Auto-receipt (Shipment.afterUpdate -> AutomaticReceiptEvent -> AutomaticReceiptJob) receives it
 *     into the sortation bin; inbound sortation (ShipmentEventService) creates two putaways: 3 to the
 *     cross-dock lane (what the backorder needs), 7 to the preferred bin.
 *  4. Handheld: the putaway tasks of the product; both completed (direct putaway, the app's body).
 *  5. Completing the CROSS_DOCK task publishes AutomaticBackorderReallocationEvent -> the job allocates
 *     the backorder from the lane: PICKING, backorder cleared.
 *  6. Browser: the sales order is issued from its stock movement page -> Shipped / Issued; the lane is empty.
 */
import { expect, knownGap, test } from '../../lib/ts/test';
import { Admin, days, Fixtures } from '../../lib/ts/fixtures';
import { MobileApp } from '../../lib/ts/app';
import { MAIN_SUPPLIER, uid } from '../../lib/ts/env';
import { mdy } from '../../lib/ts/workflows';
import { chooseLocation, sendFromWizard, stockMovementStatus } from '../../lib/ts/web';
import { eventually, rows, scalar } from '../../lib/ts/db';

async function site(fx: Fixtures, prefix: string, depotExtra: string[] = ['AUTO_RECEIVING'], sortActivities = ['INBOUND_SORTATION', 'RECEIVE_STOCK']) {
  const dc = await fx.depot(`${prefix}-DC`, ['AUTOMATED_PUTAWAY_CREATION', ...depotExtra]);
  const customer = await fx.depot(`${prefix}-CUST`);
  const sort = await fx.bin(dc, `${prefix}-SORT`, sortActivities);
  const storage = await fx.bin(dc, `${prefix}-STOR`, ['PICK_STOCK', 'PUTAWAY_STOCK']);
  const lane = await fx.bin(dc, `${prefix}-LANE`, ['CROSS_DOCKING', 'DELIVERY_TYPE_LOCAL_DELIVERY', 'PICK_STOCK', 'PUTAWAY_STOCK']);
  const p = await fx.product(prefix);
  await fx.inventoryLevel(dc, p.id, { preferredBinLocationId: storage.id });
  return { dc, customer, sort, storage, lane, p };
}

/** The Inbound wizard's calls; `backorderReference` is written onto the shipment lines before sending. */
async function inboundFor(dc: string, productId: string, quantity: number, backorderReference?: string) {
  const admin = await Admin.at(dc);
  const sm = await admin.post('/stockMovements', { name: '', description: 'e2e sortation', dateRequested: mdy(new Date()),
    origin: { id: MAIN_SUPPLIER }, destination: { id: dc }, requestedBy: { id: '1' }, stocklist: { id: '' } });
  const id: string = sm.data.id;
  await admin.post(`/stockMovements/${id}/updateItems`, { id, lineItems: [{ product: { id: productId }, quantityRequested: quantity,
    lotNumber: `XD-${uid()}`, expirationDate: mdy(days(300)), sortOrder: 100, recipient: { id: '' }, palletName: '', boxName: '' }] });
  await admin.post(`/stockMovements/${id}/status`, { status: 'CHECKING' });
  if (backorderReference) {
    await rows('UPDATE shipment_item si JOIN shipment s ON s.id = si.shipment_id SET si.backorder_reference = ? WHERE s.requisition_id = ?', [backorderReference, id]);
  }
  await admin.post(`/stockMovements/${id}/updateShipment`, { dateShipped: mdy(new Date()), shipmentType: '5', trackingNumber: '',
    driverName: '', comments: '', expectedDeliveryDate: mdy(days(1)) });
  await admin.post(`/stockMovements/${id}/status`, { status: 'DISPATCHED' });
  await admin.dispose();
  return { stockMovementId: id, shipmentId: (await scalar<string>('SELECT id FROM shipment WHERE requisition_id = ?', [id]))! };
}

type Putaway = { quantity: number; from: string; to: string; description: string };
async function putaways(dc: string): Promise<Putaway[]> {
  const r = await rows<any>(`SELECT oi.quantity, ob.location_number AS f, db.location_number AS t, oi.description AS d FROM \`order\` o
      JOIN order_item oi ON oi.order_id = o.id LEFT JOIN location ob ON ob.id = oi.origin_bin_location_id
      LEFT JOIN location db ON db.id = oi.destination_bin_location_id WHERE o.origin_id = ? AND oi.parent_order_item_id IS NULL ORDER BY oi.quantity`, [dc]);
  return r.map((x) => ({ quantity: Number(x.quantity), from: x.f, to: x.t, description: x.d }));
}

test.describe('sortation, cross-dock and backorders', () => {
  test('a backorder is cross-docked from the next inbound and released, then issued (event-driven jobs + handheld + browser)', async ({ page, fx }) => {
    test.setTimeout(240_000);
    const s = await site(fx, 'XD');

    // 1. the backorder (written by the upstream order system)
    const so = await fx.requisition(s.dc, s.customer, [{ productId: s.p.id, quantity: 3, quantityBackordered: 3 }],
      { status: 'CREATED', deliveryTypeCode: 'LOCAL_DELIVERY', autoAllocationRequested: true });

    // 2. the inbound carrying the backorder reference, sent
    const inbound = await inboundFor(s.dc, s.p.id, 10, so.requestNumber);

    await test.step('auto-receipt into the sortation bin, sortation splits cross-dock / storage', async () => {
      const received = await eventually(async () => (await fx.shipmentStatus(inbound.shipmentId)) === 'RECEIVED', 60000, 1000);
      expect(received, `shipment status ${await fx.shipmentStatus(inbound.shipmentId)}`).toBe(true);
      await expect.poll(() => putaways(s.dc), { timeout: 30000 }).toEqual([
        { quantity: 3, from: s.sort.number, to: s.lane.number, description: 'Cross-Docking' },
        { quantity: 7, from: s.sort.number, to: s.storage.number, description: 'Default Location' },
      ]);
      await expect.poll(() => fx.onHandByBin(s.dc, s.p.id), { timeout: 30000 }).toEqual({ [s.sort.number]: 10 });
      await expect.poll(() => fx.requisitionStatus(so.id), { timeout: 3000 }).toBe('CREATED');
    });

    const app = await MobileApp.signIn(s.dc);
    await test.step('handheld: complete the cross-dock and the storage putaway', async () => {
      const tasks = await app.call('src/apis/putaway.ts#getPutawayTasks', { facilityId: s.dc, productId: s.p.id });
      tasks.holds();
      const byDest = Object.fromEntries(tasks.data.data.map((t: any) => [t.destination?.locationNumber, t]));
      expect(Object.keys(byDest).sort()).toEqual([s.lane.number, s.storage.number].sort());
      for (const t of [byDest[s.lane.number], byDest[s.storage.number]]) {
        (await app.call('src/apis/putaway.ts#patchPutawayTask', { facilityId: s.dc, putawayItemId: t.id }, { action: 'start' })).holds();
        const done = await app.call('src/apis/putaway.ts#patchPutawayTask', { facilityId: s.dc, putawayItemId: t.id },
          { action: 'complete', destination: t.destination.id, isCancelRemaining: false, reasonCode: null, isDirectPutaway: true });
        done.holds();
        expect(done.data.data.status).toBe('COMPLETED');
      }
      await expect.poll(() => fx.onHandByBin(s.dc, s.p.id), { timeout: 30000 }).toEqual({ [s.lane.number]: 3, [s.storage.number]: 7 });
    });
    await app.dispose();

    await test.step('the backorder reallocation job releases the sales order from the lane', async () => {
      await expect.poll(() => fx.requisitionStatus(so.id), { timeout: 60000, intervals: [1000] }).toBe('PICKING');
      expect(await scalar('SELECT quantity_backordered FROM requisition_item WHERE id = ?', [so.itemIds[0]])).toBeNull();
      const picks = await rows<any>(`SELECT b.location_number AS bin, pi.quantity FROM picklist p JOIN picklist_item pi ON pi.picklist_id = p.id
                                      JOIN location b ON b.id = pi.bin_location_id WHERE p.requisition_id = ?`, [so.id]);
      expect(picks.map((x) => [x.bin, Number(x.quantity)])).toEqual([[s.lane.number, 3]]);
    });

    await test.step('browser: the released order goes through the wizard Pick and Send steps', async () => {
      await chooseLocation(page, s.dc);
      await sendFromWizard(page, so.id);
      expect(await stockMovementStatus(page, so.id)).toContain('Shipped');
      await expect.poll(() => fx.requisitionStatus(so.id), { timeout: 3000 }).toBe('ISSUED');
    });
    // what the shipment carries and the lane debit: the KNOWN-GAP test below
  });

  knownGap('a backorder released by the reallocation job ships its units from the cross-dock lane',
    'the job allocates without creating a shipment; the wizard Pick step (status PICKED) then calls StockMovementService.createShipment(Requisition), which creates a NEW shipment with no items (items are only added when a shipment already exists, createMissingShipmentItems), so Send ships an empty shipment: ISSUED / SHIPPED, nothing issued, the units stay in the lane',
    [], async ({ page, fx }) => {
      test.setTimeout(240_000);
      const s = await site(fx, 'XE');
      const so = await fx.requisition(s.dc, s.customer, [{ productId: s.p.id, quantity: 3, quantityBackordered: 3 }],
        { status: 'CREATED', deliveryTypeCode: 'LOCAL_DELIVERY', autoAllocationRequested: true });
      const inbound = await inboundFor(s.dc, s.p.id, 10, so.requestNumber);
      await expect.poll(() => fx.shipmentStatus(inbound.shipmentId), { timeout: 60000, intervals: [1000] }).toBe('RECEIVED');
      await expect.poll(async () => (await putaways(s.dc)).length, { timeout: 30000 }).toBe(2);
      const app = await MobileApp.signIn(s.dc);
      for (const t of (await app.call('src/apis/putaway.ts#getPutawayTasks', { facilityId: s.dc, productId: s.p.id })).data.data) {
        (await app.call('src/apis/putaway.ts#patchPutawayTask', { facilityId: s.dc, putawayItemId: t.id }, { action: 'start' })).holds();
        (await app.call('src/apis/putaway.ts#patchPutawayTask', { facilityId: s.dc, putawayItemId: t.id },
          { action: 'complete', destination: t.destination.id, isCancelRemaining: false, reasonCode: null, isDirectPutaway: true })).holds();
      }
      await app.dispose();
      await expect.poll(() => fx.requisitionStatus(so.id), { timeout: 60000, intervals: [1000] }).toBe('PICKING');
      await chooseLocation(page, s.dc);
      await sendFromWizard(page, so.id);
      const shipped = await rows<any>(`SELECT b.location_number AS bin, si.quantity FROM shipment s JOIN shipment_item si ON si.shipment_id = s.id
                                        LEFT JOIN location b ON b.id = si.bin_location_id WHERE s.requisition_id = ?`, [so.id]);
      expect(shipped.map((x) => [x.bin, Number(x.quantity)]), 'the shipment lines').toEqual([[s.lane.number, 3]]);
      await expect.poll(() => fx.onHandByBin(s.dc, s.p.id), { timeout: 30000 }).toEqual({ [s.storage.number]: 7 });
    });

  test('no backorder: the whole receipt is sorted to the preferred bin', async ({ fx }) => {
    const s = await site(fx, 'XN');
    const inbound = await inboundFor(s.dc, s.p.id, 6);
    await expect.poll(() => fx.shipmentStatus(inbound.shipmentId), { timeout: 60000, intervals: [1000] }).toBe('RECEIVED');
    await expect.poll(() => putaways(s.dc), { timeout: 30000 })
      .toEqual([{ quantity: 6, from: s.sort.number, to: s.storage.number, description: 'Default Location' }]);
  });

  knownGap('completing a putaway task that was never loaded into a cart moves the stock out of its bin',
    'PutawayTaskService.transferToDestination transfers from task.container unless isDirectPutaway; a task never loaded has no container, so complete without isDirectPutaway debits a null bin: the sortation bin keeps the stock and a -N "no bin" row appears',
    'src/apis/putaway.ts#patchPutawayTask', async ({ fx }) => {
      const s = await site(fx, 'XC');
      const inbound = await inboundFor(s.dc, s.p.id, 5);
      await expect.poll(() => fx.shipmentStatus(inbound.shipmentId), { timeout: 60000, intervals: [1000] }).toBe('RECEIVED');
      await expect.poll(async () => (await putaways(s.dc)).length, { timeout: 30000 }).toBe(1);
      const app = await MobileApp.signIn(s.dc);
      const t = (await app.call('src/apis/putaway.ts#getPutawayTasks', { facilityId: s.dc, productId: s.p.id })).data.data[0];
      (await app.call('src/apis/putaway.ts#patchPutawayTask', { facilityId: s.dc, putawayItemId: t.id }, { action: 'start' })).holds();
      const done = await app.call('src/apis/putaway.ts#patchPutawayTask', { facilityId: s.dc, putawayItemId: t.id },
        { action: 'complete', destination: t.destination.id, isCancelRemaining: false, reasonCode: null });
      await app.dispose();
      // either refused (no cart, not direct) or taken from the task's bin; never from no bin
      if (done.status < 300) {
        await expect.poll(() => fx.onHandByBin(s.dc, s.p.id), { timeout: 30000 }).toEqual({ [s.storage.number]: 5 });
      }
    });

  knownGap('receiving in the browser at a sortation depot also creates the putaways',
    'only ReceiptService.receiveInboundShipment (the automatic receipt) publishes ShipmentEvent(SHIPMENT_RECEIVED), the trigger of inbound sortation; a receipt completed in the Partial Receiving page (or the handheld POST /partialReceiving/{id}) never does, so stock received into the INBOUND_SORTATION bin by a person gets no putaway, no cross-dock (nothing logged)',
    [], async ({ page, fx }) => {
    // no AUTO_RECEIVING: a person receives the shipment in the Partial Receiving page, into the sortation
    // bin (INBOUND_SORTATION only: the page's bin picker leaves out every RECEIVE_STOCK bin)
    const s = await site(fx, 'XW', ['PARTIAL_RECEIVING'], ['INBOUND_SORTATION']);
    const inbound = await inboundFor(s.dc, s.p.id, 4);
    await chooseLocation(page, s.dc);
    await page.goto(`partialReceiving/create/${inbound.shipmentId}`);
    const row = page.getByRole('row').filter({ hasText: s.p.productCode });
    await expect(row).toBeVisible();
    const bin = row.getByRole('cell', { name: 'Bin Location' });
    await bin.getByRole('textbox').pressSequentially(s.sort.number.slice(0, 8));
    await page.locator('.react-select__option').filter({ hasText: s.sort.number }).first().click();
    await row.getByRole('cell', { name: 'Receiving now' }).getByRole('textbox').fill('4');
    await page.getByRole('button', { name: 'Next' }).click();
    await page.getByRole('button', { name: 'Receive shipment' }).click();
    await page.waitForURL(/stockMovement\/show\//);
    await expect.poll(() => fx.shipmentStatus(inbound.shipmentId), { timeout: 3000 }).toBe('RECEIVED');
    await expect.poll(() => fx.onHandByBin(s.dc, s.p.id), { timeout: 30000 }).toEqual({ [s.sort.number]: 4 });
    await expect.poll(() => putaways(s.dc), { timeout: 30000 })
      .toEqual([{ quantity: 4, from: s.sort.number, to: s.storage.number, description: 'Default Location' }]);
  });
});
