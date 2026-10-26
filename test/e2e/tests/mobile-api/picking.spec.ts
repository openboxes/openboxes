/**
 * Outbound: the picklist flow, pick tasks (start, pick into a tote, short-pick, drop at staging,
 * reallocate), packing location, Pick Up allocation. Runs in Test
 * Mobile DC (REQUIRE_MOBILE_PICKING + TRACK_INTERNAL_TRANSACTIONS). Each test builds its own order:
 * an order already in PICKING with a picklist and a shipment, written in SQL the way the web
 * workflow leaves it (the workflow API at a mobile-picking depot stops before the shipment exists;
 * the flows project drives that workflow itself).
 */
import { contractTest, expect, knownGap, test } from '../../lib/ts/test';
import { MobileApp } from '../../lib/ts/app';
import { uid } from '../../lib/ts/env';
import { days, Fixtures } from '../../lib/ts/fixtures';
import { committed, rows, scalar } from '../../lib/ts/db';

type Order = {
  dc: string; destination: string; bin: { id: string; number: string }; tote: { id: string; number: string };
  staging: { id: string; number: string }; packing: { id: string; number: string };
  product: { id: string; productCode: string }; lot: { id: string; lotNumber: string; expiry: Date };
  requisition: { id: string; requestNumber: string; itemIds: string[] }; taskIds: string[]; picklistId: string; shipmentId: string;
};

const DELIVERY = 'DELIVERY_TYPE_LOCAL_DELIVERY';

async function pickOrder(fx: Fixtures, quantity = 5, lines = 1): Promise<Order> {
  const dc = await fx.mobileDc();
  const destination = await fx.depot('PK-DEST');
  const bin = await fx.bin(dc, 'PK-BIN', ['PICK_STOCK', 'PUTAWAY_STOCK']);
  const tote = await fx.bin(dc, 'PK-TOTE', ['OUTBOUND_CONTAINER', 'HOLD_STOCK', DELIVERY]);
  const staging = await fx.bin(dc, 'PK-STAGE', ['STAGING_LOCATION', 'HOLD_STOCK', DELIVERY]);
  const packing = await fx.bin(dc, 'PK-PACK', ['PACK_STOCK']);
  const product = await fx.product('PK');
  const lotNumber = `LOT-${uid()}`;
  const expiry = days(365);
  await fx.stock(dc, product.id, [[bin.id, quantity * lines * 2, lotNumber, expiry]]);
  const lot = { id: await fx.lotId(product.id, lotNumber), lotNumber, expiry };
  const requisition = await fx.requisition(dc, destination, [{ productId: product.id, quantity: quantity * lines }],
    { status: 'PICKING', deliveryTypeCode: 'LOCAL_DELIVERY', requestNumber: `LP${uid()}` });
  const pl = await fx.picklist(requisition.id, Array.from({ length: lines }, () => [requisition.itemIds[0], lot.id, bin.id, quantity] as [string, string, string, number]));
  const shipment = await fx.shipment(dc, destination, { requisitionId: requisition.id, number: requisition.requestNumber, name: `Test pick ${requisition.requestNumber}` });
  return { dc, destination, bin, tote, staging, packing, product, lot, requisition, taskIds: [...pl.itemIds].sort(), picklistId: pl.picklistId, shipmentId: shipment.id };
}

/** A Pick Up order waiting for allocation (CREATED), stock in a storage and a display bin. */
async function pickUpOrder(fx: Fixtures, quantity = 8) {
  const dc = await fx.mobileDc();
  const destination = await fx.depot('PU-DEST');
  const bin = await fx.bin(dc, 'PU-STOR', ['PICK_STOCK', 'PUTAWAY_STOCK']);
  const display = await fx.bin(dc, 'PU-DISP', ['PICK_STOCK', 'PUTAWAY_STOCK', 'DISPLAY_STOCK']);
  const product = await fx.product('PU');
  const lotNumber = `LOT-${uid()}`;
  await fx.stock(dc, product.id, [[bin.id, 10, lotNumber, days(365)], [display.id, 5, `${lotNumber}-D`, days(200)]]);
  const lot = { id: await fx.lotId(product.id, lotNumber), lotNumber };
  const requisition = await fx.requisition(dc, destination, [{ productId: product.id, quantity }],
    { status: 'CREATED', deliveryTypeCode: 'PICK_UP', autoAllocationRequested: false, requestNumber: `PU${uid()}` });
  return { dc, destination, bin, display, product, lot, requisition };
}

test.describe('picking', () => {
  let app: MobileApp;
  test.beforeEach(async ({ fx }) => { app = await MobileApp.signIn(await fx.mobileDc()); });
  test.afterEach(async () => { await app.dispose(); });

  async function task(o: Order, taskId: string) {
    const r = await app.call('src/apis/picking.ts#getPickTaskByIdApi', { facilityId: o.dc, taskId });
    expect(r.status, r.toString()).toBe(200);
    return r.data.data;
  }
  const start = (o: Order, i = 0) => app.call('src/apis/picking.ts#patchPickTaskApi', { facilityId: o.dc, taskId: o.taskIds[i] }, { action: 'start', assigneeId: '1' });
  const pick = (o: Order, i = 0) => app.call('src/apis/picking.ts#patchPickTaskApi', { facilityId: o.dc, taskId: o.taskIds[i] },
    { action: 'pick', outboundContainerId: o.tote.id, pickedById: '1' });

  // ------------------------------------------------------------------ picklist flow (orders.ts)
  contractTest('GET /stockMovements (outbound, PICKING) lists the order with the fields the order list reads', 'src/apis/orders.ts#getOrders', async ({ fx }) => {
    const o = await pickOrder(fx);
    (await app.call('src/apis/orders.ts#submitPackingLocation', { id: o.requisition.id }, { 'packingLocation.id': o.packing.id })).holds();
    const r = await app.call('src/apis/orders.ts#getOrders', { 'global.location.id': o.dc });
    r.rows((x) => x.id === o.requisition.id).holds();
    expect(r.data.data.find((x: any) => x.id === o.requisition.id).packingLocation.id).toBe(o.packing.id);
  });

  contractTest('GET /picklists/{id} answers the picklist with the fields the picklist screen reads', 'src/apis/orders.ts#getPickList', async ({ fx }) => {
    const o = await pickOrder(fx);
    (await app.call('src/apis/orders.ts#getPickList', { id: o.picklistId })).holds();
  });

  contractTest('GET /picklistItems/{id} answers the picklist item (saga wired, never dispatched)', 'src/apis/orders.ts#getPickListItem', async ({ fx }) => {
    const o = await pickOrder(fx);
    const r = await app.call('src/apis/orders.ts#getPickListItem', { id: o.taskIds[0] });
    r.holds();
    expect(r.data.data.id).toBe(o.taskIds[0]);
  });

  contractTest('GET /stockMovements?direction&status today: 400 (dead path, characterised)', 'src/apis/orders.ts#getStockMovement', async ({ fx }) => {
    await pickOrder(fx);
    const r = await app.call('src/apis/orders.ts#getStockMovement', { direction: 'OUTBOUND', status: 'PICKING' });
    expect(r.status).toBe(400);
  }, { characterise: '400 Origin and destination cannot be the same' });

  knownGap('GET /stockMovements?direction&status answers the outbound list',
    '(dead path) getStockMovement sends direction+status but no origin/destination: 400 "Origin and destination cannot be the same", and `status` is ignored',
    'src/apis/orders.ts#getStockMovement', async ({ fx }) => {
      await pickOrder(fx);
      (await app.call('src/apis/orders.ts#getStockMovement', { direction: 'OUTBOUND', status: 'PICKING' })).holds();
    });

  contractTest('POST /stockMovements/{id}/packingLocation with the flat packingLocation.id key schedules packing', 'src/apis/orders.ts#submitPackingLocation', async ({ fx }) => {
    const o = await pickOrder(fx);
    (await app.call('src/apis/orders.ts#submitPackingLocation', { id: o.requisition.id }, { 'packingLocation.id': o.packing.id })).holds();
  });

  contractTest('POST /picklistItems/{id} with the picklist screen body records the picked quantity', 'src/apis/orders.ts#submitPickListItem', async ({ fx }) => {
    const o = await pickOrder(fx);
    (await app.call('src/apis/orders.ts#submitPickListItem', { id: o.taskIds[0] },
      { 'product.id': o.product.id, productCode: o.product.productCode, quantityPicked: 2, shortage: false })).holds();
    expect(Number(await scalar('SELECT quantity_picked FROM picklist_item WHERE id = ?', [o.taskIds[0]]))).toBe(2);
  });

  contractTest('GET /stockMovements/{id} answers the stock movement (saga wired, never dispatched)', 'src/apis/transfer.ts#getStockMovements', async ({ fx }) => {
    const o = await pickOrder(fx);
    (await app.call('src/apis/transfer.ts#getStockMovements', { id: o.requisition.id })).holds();
  });

  // ------------------------------------------------------------------ pick tasks (picking.ts)
  contractTest('GET /facilities/{f}/pick-tasks (queue: unassigned, by delivery type) lists the open task', 'src/apis/picking.ts#getPickTasksApi', async ({ fx }) => {
    const o = await pickOrder(fx);
    const r = await app.call('src/apis/picking.ts#getPickTasksApi', { facilityId: o.dc, 'params.deliveryTypeCode': 'LOCAL_DELIVERY' });
    r.rows((x) => x.requisitionId === o.requisition.id).holds();
    expect(r.data.data.map((x: any) => x.id)).toContain(o.taskIds[0]);
  });

  contractTest('GET /facilities/{f}/pick-tasks with ordersCount batches whole orders', 'src/apis/picking.ts#getPickTasksApi', async ({ fx }) => {
    const o = await pickOrder(fx);
    // ordersCount takes whole orders oldest first and the database keeps every earlier test's open
    // orders, so ask for enough orders to reach this (newest) one
    const r = await app.call('src/apis/picking.ts#getPickTasksApi', { facilityId: o.dc, 'params.deliveryTypeCode': 'LOCAL_DELIVERY', 'params.ordersCount': 1000 });
    r.rows((x) => x.requisitionId === o.requisition.id).holds();
  });

  contractTest('GET /facilities/{f}/pick-tasks?status=PENDING&status=PICKING lists the discrete order rows', 'src/apis/picking.ts#getOpenPickTasksApi', async ({ fx }) => {
    const o = await pickOrder(fx);
    const r = await app.call('src/apis/picking.ts#getOpenPickTasksApi', { facilityId: o.dc, excludeAssignedRequisitions: true });
    r.rows((x) => x.requisitionId === o.requisition.id).holds();
    expect(r.data.data.find((x: any) => x.id === o.taskIds[0])?.requisitionId).toBe(o.requisition.id);
  });

  contractTest('GET /facilities/{f}/pick-tasks/counts answers a count per delivery type', 'src/apis/picking.ts#getPickTaskCountsApi', async ({ fx }) => {
    const o = await pickOrder(fx);
    const r = await app.call('src/apis/picking.ts#getPickTaskCountsApi', { facilityId: o.dc });
    r.holds();
    expect(r.data.data.find((x: any) => x.deliveryTypeCode === 'LOCAL_DELIVERY').availableCount).toBeGreaterThanOrEqual(1);
  });

  knownGap('GET /facilities/{f}/pick-tasks/counts sends a totalCount per delivery type',
    'the app types counts as {deliveryTypeCode, availableCount, totalCount}; PickTaskApiController.counts sends no totalCount (not read today)',
    'src/apis/picking.ts#getPickTaskCountsApi', async ({ fx }) => {
      const o = await pickOrder(fx);
      const r = await app.call('src/apis/picking.ts#getPickTaskCountsApi', { facilityId: o.dc });
      expect(r.data.data.every((x: any) => typeof x.totalCount === 'number')).toBe(true);
    });

  contractTest('GET /facilities/{f}/pick-tasks?requisitionId&status lists the order tasks', 'src/apis/picking.ts#getPickTasksByRequisitionApi', async ({ fx }) => {
    const o = await pickOrder(fx, 5, 2);
    const r = await app.call('src/apis/picking.ts#getPickTasksByRequisitionApi', { facilityId: o.dc, requisitionId: o.requisition.id, 'statuses[]': ['PENDING', 'PICKING'] });
    r.holds();
    expect(r.data.data.map((x: any) => x.id).sort()).toEqual(o.taskIds);
  });

  contractTest('GET /facilities/{f}/pick-tasks/{id} answers the task the picking screen reads', 'src/apis/picking.ts#getPickTaskByIdApi', async ({ fx }) => {
    const o = await pickOrder(fx);
    const r = await app.call('src/apis/picking.ts#getPickTaskByIdApi', { facilityId: o.dc, taskId: o.taskIds[0] });
    r.holds();
    expect(r.data.data.stagingLocation.id).toBeTruthy();
  });

  contractTest('PATCH pick-tasks/{id} start then pick moves the task into the tote', 'src/apis/picking.ts#patchPickTaskApi', async ({ fx }) => {
    const o = await pickOrder(fx);
    const s = await start(o);
    const p = await pick(o);
    const after = await task(o, o.taskIds[0]);
    s.holds(); p.holds();
    expect(after.status).toBe('PICKED');
    expect(after.outboundContainer.id).toBe(o.tote.id);
    expect(after.assignee.firstName).toBeTruthy();
  });

  contractTest('PATCH pick-tasks/{id} short-pick with a reason code name records the shortage', 'src/apis/picking.ts#patchPickTaskApi', async ({ fx }) => {
    const o = await pickOrder(fx);
    (await start(o)).holds();
    const r = await app.call('src/apis/picking.ts#patchPickTaskApi', { facilityId: o.dc, taskId: o.taskIds[0] },
      { action: 'short-pick', outboundContainerId: o.tote.id, quantityPicked: 3, pickedById: '1', reasonCode: 'STOCKOUT' });
    r.holds();
    expect((await task(o, o.taskIds[0])).reasonCode).toBe('STOCKOUT');
  });

  contractTest('PATCH pick-tasks/{id} short-pick of zero with no tote stages the started task', 'src/apis/picking.ts#patchPickTaskApi', async ({ fx }) => {
    const o = await pickOrder(fx, 5, 2);
    (await start(o)).holds();
    (await app.call('src/apis/picking.ts#patchPickTaskApi', { facilityId: o.dc, taskId: o.taskIds[0] },
      { action: 'short-pick', outboundContainerId: null, quantityPicked: 0, pickedById: '1', reasonCode: 'STOCKOUT' })).holds();
  });

  contractTest('GET pick-tasks?outboundContainerId&status=PICKED lists what is in the tote', 'src/apis/picking.ts#getPickTasksByStatusAndContainerApi', async ({ fx }) => {
    const o = await pickOrder(fx);
    (await start(o)).holds();
    (await pick(o)).holds();
    const r = await app.call('src/apis/picking.ts#getPickTasksByStatusAndContainerApi', { facilityId: o.dc, outboundContainerId: o.tote.id, status: 'PICKED' });
    r.holds();
    expect(r.data.data.map((x: any) => x.id)).toEqual([o.taskIds[0]]);
  });

  contractTest('PATCH pick-tasks/containers/{tote} drop stages the picked tote', 'src/apis/picking.ts#dropPickTaskApi', async ({ fx }) => {
    const o = await pickOrder(fx);
    (await start(o)).holds();
    (await pick(o)).holds();
    await fx.awaitOnHand(o.dc, o.product.id, 10);
    await expect.poll(async () => (await fx.onHandByBin(o.dc, o.product.id))[o.tote.number] ?? 0, { timeout: 30000 }).toBe(5);
    const stagingId = (await task(o, o.taskIds[0])).stagingLocation.id;
    const r = await app.call('src/apis/picking.ts#dropPickTaskApi', { facilityId: o.dc, outboundContainerId: o.tote.id },
      { action: 'drop', stagingLocationId: stagingId, stagedById: '1' });
    r.holds();
    expect(await committed('SELECT status FROM picklist_item WHERE id = ?', [o.taskIds[0]], 'STAGED')).toBe('STAGED');
  });

  contractTest('POST pick-tasks/{id}/reallocate with the reallocate modal body moves the task to another bin', 'src/apis/picking.ts#reallocatePickTaskApi', async ({ fx }) => {
    const o = await pickOrder(fx);
    const other = await fx.bin(o.dc, 'PK-ALT', ['PICK_STOCK', 'PUTAWAY_STOCK']);
    await fx.stock(o.dc, o.product.id, [[o.bin.id, 10, o.lot.lotNumber, o.lot.expiry], [other.id, 10, o.lot.lotNumber, o.lot.expiry]]);
    const r = await app.call('src/apis/picking.ts#reallocatePickTaskApi', { facilityId: o.dc, taskId: o.taskIds[0] },
      { picklistItems: [{ inventoryItem: { id: o.lot.id }, binLocation: { id: other.id }, quantity: 5 }] });
    r.holds();
    const inResponse = r.data.data.some((x: any) => x.location?.id === other.id);
    const db = await rows('SELECT bin_location_id, quantity, status FROM picklist_item WHERE requisition_item_id = ?', [o.requisition.itemIds[0]]);
    expect(inResponse, `reallocate answered ${JSON.stringify(r.data).slice(0, 1500)}; picklist_item rows now ${JSON.stringify(db)}; other bin ${other.id}`).toBe(true);
  });

  // ------------------------------------------------------------------ item details + Pick Up allocation
  contractTest('GET /stockMovementItems/{id}/details?stepNumber=4 answers available stock and the allocation', 'src/apis/picking.ts#getStockMovementItemDetailsApi', async ({ fx }) => {
    const o = await pickOrder(fx);
    (await app.call('src/apis/picking.ts#getStockMovementItemDetailsApi', { requisitionItemId: o.requisition.itemIds[0] })).holds();
  });

  contractTest('GET /stockMovements (Pick Up, CREATED/VERIFYING) lists the order waiting for allocation', 'src/apis/pua.ts#getOutboundOrders', async ({ fx }) => {
    const o = await pickUpOrder(fx);
    const r = await app.call('src/apis/pua.ts#getOutboundOrders', { 'global.location.id': o.dc });
    r.rows((x) => x.id === o.requisition.id).holds();
    expect(r.data.data.find((x: any) => x.id === o.requisition.id)?.lineItemCount).toBe(1);
  });

  contractTest('POST /outbound-orders/{id}/items/{itemId}/allocations AUTO with the app strategies allocates', 'src/apis/pua.ts#allocate', async ({ fx }) => {
    const o = await pickUpOrder(fx);
    const r = await app.call('src/apis/pua.ts#allocate', { orderId: o.requisition.id, lineItemId: o.requisition.itemIds[0] }, { mode: 'AUTO', strategies: ['WAREHOUSE_FIRST'] });
    const display = await app.call('src/apis/pua.ts#allocate', { orderId: o.requisition.id, lineItemId: o.requisition.itemIds[0] }, { mode: 'AUTO', strategies: ['DISPLAY_FIRST'] });
    r.holds(); display.holds();
  });

  contractTest('POST .../allocations MANUAL with the app allocation rows allocates exactly those', 'src/apis/pua.ts#allocate', async ({ fx }) => {
    const o = await pickUpOrder(fx);
    const r = await app.call('src/apis/pua.ts#allocate', { orderId: o.requisition.id, lineItemId: o.requisition.itemIds[0] },
      { mode: 'MANUAL', allocations: [{ inventoryItemId: o.lot.id, binLocationId: o.bin.id, quantity: 8 }] });
    const details = await app.call('src/apis/pua.ts#getOutboundOrderDetails', { id: o.requisition.id });
    r.holds();
    expect(details.data.data.lineItems[0].allocations.map((a: any) => a.quantity)).toEqual([8]);
  });

  contractTest('GET /outbound-orders/{id} answers lines, available stock and allocations', 'src/apis/pua.ts#getOutboundOrderDetails', async ({ fx }) => {
    const o = await pickUpOrder(fx);
    (await app.call('src/apis/pua.ts#allocate', { orderId: o.requisition.id, lineItemId: o.requisition.itemIds[0] },
      { mode: 'MANUAL', allocations: [{ inventoryItemId: o.lot.id, binLocationId: o.bin.id, quantity: 4 }] })).holds();
    (await app.call('src/apis/pua.ts#getOutboundOrderDetails', { id: o.requisition.id })).holds();
  });

  contractTest('POST /stockMovements/{id}/status PICKING releases the allocated Pick Up order to picking', 'src/apis/pua.ts#updateOrderStatus', async ({ fx }) => {
    const o = await pickUpOrder(fx);
    (await app.call('src/apis/pua.ts#allocate', { orderId: o.requisition.id, lineItemId: o.requisition.itemIds[0] }, { mode: 'AUTO', strategies: ['WAREHOUSE_FIRST'] })).holds();
    (await app.call('src/apis/pua.ts#updateOrderStatus', { orderId: o.requisition.id }, { status: 'PICKING' })).holds();
    await expect.poll(() => fx.requisitionStatus(o.requisition.id), { timeout: 3000 }).toBe('PICKING');
  });
});
