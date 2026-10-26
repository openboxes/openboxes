/**
 * Putaway: the putaway order flow (candidates, create, list, complete) and the putaway-task flow
 * (sortation: start, load into a cart; putaway: complete, partial complete, cancel remaining;
 * alternate destinations; reason codes). Test Mobile DC.
 */
import { contractTest, expect, knownGap, test } from '../../lib/ts/test';
import { MobileApp } from '../../lib/ts/app';
import { uid } from '../../lib/ts/env';
import { days } from '../../lib/ts/fixtures';
import { scalar } from '../../lib/ts/db';

test.describe('putaway', () => {
  let app: MobileApp;
  let dc: string;
  let receiving: { id: string; number: string };
  let storage: { id: string; number: string };
  let cart: { id: string; number: string };
  let p: { id: string; productCode: string };
  let lot: { id: string; lotNumber: string };

  test.beforeEach(async ({ fx }) => {
    dc = await fx.mobileDc();
    app = await MobileApp.signIn(dc);
    receiving = await fx.bin(dc, 'PA-RECV', ['RECEIVE_STOCK']);
    storage = await fx.bin(dc, 'PA-STOR', ['PUTAWAY_STOCK', 'PICK_STOCK']);
    cart = await fx.bin(dc, 'PA-CART', ['PUTAWAY_CART']);
    const lnf = await scalar<number>(`SELECT COUNT(*) FROM location l JOIN location_supported_activities a ON a.location_id = l.id
                                       WHERE l.parent_location_id = ? AND a.supported_activities_string = 'LOST_AND_FOUND'`, [dc]);
    if (!Number(lnf)) await fx.bin(dc, 'PA-LNF', ['LOST_AND_FOUND']);
    p = await fx.product('PA');
    const lotNumber = `LOT-${uid()}`;
    await fx.stock(dc, p.id, [[receiving.id, 10, lotNumber, days(365)]]);
    lot = { id: await fx.lotId(p.id, lotNumber), lotNumber };
  });
  test.afterEach(async () => { await app.dispose(); });

  /** The PutawayItem screen's body (src/screens/PutawayItem/index.tsx). */
  const createBody = (qty = 10) => ({
    putawayNumber: '', putawayStatus: 'PENDING', putawayDate: '', putawayAssignee: '', origin: dc, destination: dc,
    putawayItems: [{ putawayStatus: 'PENDING', product: p.id, inventoryItem: lot.id, putawayFacility: dc, currentLocation: receiving.id,
      putawayLocation: storage.id, quantity: qty }],
    orderedBy: '', sortBy: null,
  });
  async function createPutaway(qty = 10) {
    const r = await app.call('src/apis/putaway.ts#createPutawayOder', {}, createBody(qty));
    r.holds();
    return r.data.data;
  }
  const taskId = (putaway: any) => putaway.putawayItems[0].id as string;
  async function patch(id: string, body: unknown) {
    const r = await app.call('src/apis/putaway.ts#patchPutawayTask', { facilityId: dc, putawayItemId: id }, body);
    r.holds();
    return r.data.data;
  }

  contractTest('GET /reason-codes answers the putaway discrepancy and picking shortage codes', 'src/apis/others.ts#getReasonCodesByActivity', async () => {
    (await app.call('src/apis/others.ts#getReasonCodesByActivity', { activityCode: 'PUTAWAY_DISCREPANCY' })).holds();
    (await app.call('src/apis/others.ts#getReasonCodesByActivity', { activityCode: 'PICKING_SHORTAGE' })).holds();
  });

  contractTest('GET /locations/{id}/putawayCandidates lists received stock ready to put away', 'src/apis/putaway.ts#getCandidates', async () => {
    const r = await app.call('src/apis/putaway.ts#getCandidates', { locationId: dc });
    r.rows((x) => x['product.id'] === p.id).holds();
    expect(r.data.data.some((x: any) => x['product.id'] === p.id && x.putawayStatus === 'READY' && x.quantity === 10)).toBe(true);
  });

  contractTest('POST /putaways with the putaway item screen body creates a PENDING putaway', 'src/apis/putaway.ts#createPutawayOder', async () => {
    (await app.call('src/apis/putaway.ts#createPutawayOder', {}, createBody())).holds();
  });

  contractTest('GET /mobile/putaways lists the pending putaway', 'src/apis/putaway.ts#fetchPutAwayFromOrder', async () => {
    const created = await createPutaway();
    const r = await app.call('src/apis/putaway.ts#fetchPutAwayFromOrder', {});
    r.rows((x) => x.id === created.id).holds();
  });

  knownGap('GET /mobile/putaways?q= narrows the list to the searched putaway',
    'PutawayApiController.mobilePutaways ignores q and answers every PENDING putaway of the location',
    'src/apis/putaway.ts#fetchPutAwayFromOrder', async () => {
      const wanted = await createPutaway(5);
      const other = await createPutaway(5);
      const r = await app.call('src/apis/putaway.ts#fetchPutAwayFromOrder', { q: wanted.putawayNumber });
      r.holds();
      const ids = r.data.data.map((x: any) => x.id);
      expect(ids).toContain(wanted.id);
      expect(ids).not.toContain(other.id);
    });

  contractTest('POST /putaways/{itemId} with the completed putaway body completes the putaway (302 followed as the app does)', 'src/apis/putaway.ts#submitPutawayItem', async ({ fx }) => {
    const created = await createPutaway();
    const item = created.putawayItems[0];
    const body = {
      id: created.id, putawayNumber: created.putawayNumber, putawayStatus: 'COMPLETED', putawayDate: '', putawayAssignee: '',
      origin: created['origin.id'], destination: created['destination.id'],
      putawayItems: [{ id: item.id, putawayStatus: 'COMPLETED', currentFacility: item['currentFacility.id'], currentLocation: item['currentLocation.id'],
        product: item['product.id'], inventoryItem: item['inventoryItem.id'], putawayFacility: item['putawayFacility.id'],
        putawayLocation: item['putawayLocation.id'], quantity: item.quantity, scannedPutawayLocation: storage.number }],
      orderedBy: '', sortBy: '',
    };
    const r = await app.call('src/apis/putaway.ts#submitPutawayItem', { id: item.id }, body);
    r.holds();
    await expect.poll(() => fx.orderStatus(created.id), { timeout: 3000 }).toBe('COMPLETED');
  });

  contractTest('GET putaway-tasks?statusCategory=OPEN&product= lists the open task for the scanned product', 'src/apis/putaway.ts#getPutawayTasks', async () => {
    const created = await createPutaway();
    const r = await app.call('src/apis/putaway.ts#getPutawayTasks', { facilityId: dc, productId: p.id });
    r.rows((x) => x.id === taskId(created)).holds();
  });

  contractTest('sortation: start, load into a cart, list the cart, complete to the destination', ['src/apis/putaway.ts#patchPutawayTask', 'src/apis/putaway.ts#getPutawayDetails'], async () => {
    const id = taskId(await createPutaway());
    const started = await patch(id, { action: 'start' });
    await patch(id, { action: 'load', quantity: 10, container: cart.number, override: true });
    const inCart = await app.call('src/apis/putaway.ts#getPutawayDetails', { facilityId: dc, containerId: cart.id });
    const t = inCart.data.data.find((x: any) => x.inventoryItem?.product?.id === p.id);
    const done = await patch(t.id, { action: 'complete', destination: storage.id, isCancelRemaining: false, reasonCode: null });
    expect(started.status).toBe('STARTED');
    inCart.rows((x) => x.inventoryItem?.product?.id === p.id).holds();
    expect(t.status).toBe('IN_PROGRESS');
    expect(done.status).toBe('COMPLETED');
  });

  contractTest('direct putaway: partial complete, then complete the rest with cancel remaining', 'src/apis/putaway.ts#patchPutawayTask', async () => {
    const id = taskId(await createPutaway());
    await patch(id, { action: 'start' });
    const remaining = await patch(id, { action: 'partialComplete', quantity: 6, destination: storage.id, reasonCode: 'DAMAGED', isDirectPutaway: true });
    const done = await patch(remaining.id, { action: 'complete', destination: remaining.destination?.id, isCancelRemaining: true, reasonCode: 'DAMAGED', isDirectPutaway: true });
    expect(remaining.id).toBeTruthy();
    expect(done.status).toBe('COMPLETED');
  });

  contractTest('GET putaway-tasks/{id}/alternate-destinations answers the product bins', 'src/apis/locations.ts#getAlternativeDestinations', async ({ fx }) => {
    const id = taskId(await createPutaway());
    const other = await fx.bin(dc, 'PA-ALT', ['PUTAWAY_STOCK', 'PICK_STOCK']);
    await fx.inventoryLevel(dc, p.id, { internalLocationId: other.id });
    const r = await app.call('src/apis/locations.ts#getAlternativeDestinations', { facilityId: dc, taskId: id });
    r.holds();
    expect(r.data.data.map((x: any) => x.id)).toContain(other.id);
  });
});
