/**
 * Flow 2: pick, pack and ship an outbound order.
 *
 * A. Handheld picking at a REQUIRE_MOBILE_PICKING + TRACK_INTERNAL_TRANSACTIONS depot:
 *    API (Outbound wizard calls) creates the order; Browser (stock movement page, Scheduling tab) sets
 *    the delivery type; API releases it to picking (EditPage "Next"); Handheld (mobile contract calls)
 *    lists the order's pick task, starts it, picks into a tote, drops the tote at the staging lane;
 *    Browser issues the STAGED order ("Issue") -> Shipped / Issued, stock leaves the staging lane.
 * B. Packing on the handheld and sending from the web wizard at an ordinary depot:
 *    API creates and autopicks the order (PICKED); Handheld lists it as ready to pack, creates an LPN,
 *    packs the line into it; Browser opens the wizard's Send step, sends the shipment -> Shipped,
 *    the LPN line is on the shipment, stock is debited.
 * Known gaps found by this flow: a web-wizard order, delivery type DEFAULT, can never
 * be picked or staged on the handheld; a drop right after the pick answers "tote is empty" until the
 * asynchronous availability refresh lands.
 */
import { expect, knownGap, test } from '../../lib/ts/test';
import { Admin, days, Fixtures } from '../../lib/ts/fixtures';
import { MobileApp } from '../../lib/ts/app';
import { createOutbound, mdy, releaseToPicking } from '../../lib/ts/workflows';
import { chooseLocation, sendFromWizard, stockMovementStatus } from '../../lib/ts/web';
import { rows, scalar, committed } from '../../lib/ts/db';

const LD = 'DELIVERY_TYPE_LOCAL_DELIVERY';

async function mobileSite(fx: Fixtures, prefix: string) {
  const dc = await fx.depot(`${prefix}-DC`, ['REQUIRE_MOBILE_PICKING', 'TRACK_INTERNAL_TRANSACTIONS']);
  const customer = await fx.depot(`${prefix}-CUST`);
  const storage = await fx.bin(dc, `${prefix}-STOR`, ['PICK_STOCK', 'PUTAWAY_STOCK']);
  const tote = await fx.bin(dc, `${prefix}-TOTE`, ['OUTBOUND_CONTAINER', 'HOLD_STOCK', LD]);
  const staging = await fx.bin(dc, `${prefix}-STAGE`, ['STAGING_LOCATION', 'HOLD_STOCK', LD]);
  const p = await fx.product(prefix);
  await fx.stock(dc, p.id, [[storage.id, 20, `${prefix}-L1`, days(300)]]);
  return { dc, customer, storage, tote, staging, p };
}

test.describe('pick, pack and ship', () => {
  test('A: handheld pick into a tote, drop at staging, issue from the web app', async ({ page, fx }) => {
    const s = await mobileSite(fx, 'PPS');
    const admin = await Admin.at(s.dc);
    const order = await createOutbound(admin, s.dc, s.customer, [{ productId: s.p.id, quantity: 6 }]);
    await chooseLocation(page, s.dc);

    await test.step('browser: Scheduling tab sets the delivery type (the wizard has no such field)', async () => {
      await page.goto(`stockMovement/show/${order.id}`);
      await page.getByRole('link', { name: 'Scheduling' }).click();
      const form = page.locator('form[action*="saveSchedule"]');
      await form.locator('select[name="deliveryTypeCode"]').selectOption('LOCAL_DELIVERY', { force: true });
      await form.getByRole('button', { name: 'Save' }).click();
      await page.waitForLoadState('networkidle');
      expect(await committed('SELECT delivery_type_code FROM requisition WHERE id = ?', [order.id], 'LOCAL_DELIVERY')).toBe('LOCAL_DELIVERY');
    });

    await releaseToPicking(admin, order.id);
    await admin.dispose();
    const app = await MobileApp.signIn(s.dc);

    await test.step('handheld: the order is in the pick queue; start, pick into the tote', async () => {
      const queue = await app.call('src/apis/picking.ts#getPickTasksApi', { facilityId: s.dc, 'params.deliveryTypeCode': 'LOCAL_DELIVERY' });
      queue.rows((t) => t.requisitionId === order.id).holds();
      const tasks = await app.call('src/apis/picking.ts#getPickTasksByRequisitionApi', { facilityId: s.dc, requisitionId: order.id, 'statuses[]': ['PENDING', 'PICKING'] });
      tasks.holds();
      expect(tasks.data.data).toHaveLength(1);
      const task = tasks.data.data[0];
      expect(task.quantityRequired ?? task.quantity).toBe(6);
      (await app.call('src/apis/picking.ts#patchPickTaskApi', { facilityId: s.dc, taskId: task.id }, { action: 'start', assigneeId: '1' })).holds();
      (await app.call('src/apis/picking.ts#patchPickTaskApi', { facilityId: s.dc, taskId: task.id },
        { action: 'pick', outboundContainerId: s.tote.id, pickedById: '1' })).holds();
      // the drop reads product availability, refreshed asynchronously after the pick
      await expect.poll(async () => (await fx.onHandByBin(s.dc, s.p.id))[s.tote.number] ?? 0, { timeout: 30000 }).toBe(6);
      const fresh = await app.call('src/apis/picking.ts#getPickTaskByIdApi', { facilityId: s.dc, taskId: task.id });
      expect(fresh.data.data.stagingLocation.id).toBe(s.staging.id);
      (await app.call('src/apis/picking.ts#dropPickTaskApi', { facilityId: s.dc, outboundContainerId: s.tote.id },
        { action: 'drop', stagingLocationId: s.staging.id, stagedById: '1' })).holds();
      await expect.poll(() => fx.requisitionStatus(order.id), { timeout: 3000 }).toBe('STAGED');
      await expect.poll(() => fx.onHandByBin(s.dc, s.p.id), { timeout: 30000 }).toEqual({ [s.storage.number]: 14, [s.staging.number]: 6 });
    });
    await app.dispose();

    await test.step('browser: issue the staged order', async () => {
      await page.goto(`stockMovement/show/${order.id}`);
      const issue = page.locator(`a[href$="/requisition/issue/${order.id}"]`);
      await page.goto((await issue.getAttribute('href'))!.replace(/^\/openboxes\//, ''));
      await expect(page.getByRole('status')).toContainText(`issued requisition ${order.identifier}`);
      const status = await stockMovementStatus(page, order.id);
      expect(status).toContain('Shipped');
      expect(status).toContain('Issued');
    });
    await expect.poll(() => fx.onHandByBin(s.dc, s.p.id), { timeout: 30000 }).toEqual({ [s.storage.number]: 14 });
  });

  test('B: autopicked order packed into an LPN on the handheld, sent from the web wizard', async ({ page, fx }) => {
    const dc = await fx.depot('PPB-DC');
    const customer = await fx.depot('PPB-CUST');
    const storage = await fx.bin(dc, 'PPB-STOR', ['PICK_STOCK', 'PUTAWAY_STOCK']);
    const p = await fx.product('PPB');
    await fx.stock(dc, p.id, [[storage.id, 20, 'PPB-L1', days(300)]]);
    const admin = await Admin.at(dc);
    const order = await createOutbound(admin, dc, customer, [{ productId: p.id, quantity: 6 }]);
    await releaseToPicking(admin, order.id);
    await admin.post(`/stockMovements/${order.id}/status`, { status: 'PICKED' }); // PickPage "Next"
    await admin.dispose();
    const shipmentId = (await scalar<string>('SELECT id FROM shipment WHERE requisition_id = ?', [order.id]))!;
    const app = await MobileApp.signIn(dc);
    let lpnNumber = '';

    await test.step('handheld: ready to pack, new LPN, pack the line', async () => {
      (await app.call('src/apis/packing.ts#getShipmentsReadyToBePacked', { locationId: dc, shipmentStatusCode: 'PENDING' })).rows((x) => x.id === shipmentId).holds();
      const shipment = await app.call('src/apis/packing.ts#getShipment', { id: shipmentId });
      shipment.holds([], ['data.availableContainers', 'data.containers[].shipmentItems[].quantityRemaining']);
      const items = await rows<{ id: string; quantity: number }>('SELECT id, quantity FROM shipment_item WHERE shipment_id = ?', [shipmentId]);
      expect(items.map((i) => Number(i.quantity))).toEqual([6]);
      lpnNumber = `LPN-${order.identifier}-1`;
      const lpn = await app.call('src/apis/lpn.ts#saveAndUpdateLpn', {}, { name: lpnNumber, containerNumber: lpnNumber, 'containerType.id': '2', 'shipment.id': shipmentId });
      lpn.holds();
      (await app.call('src/apis/packing.ts#submitShipmentItems', { id: items[0].id }, { action: 'PACK', 'container.id': lpn.data.data.id, quantityToPack: 6 })).holds();
      expect(await committed('SELECT container_id FROM shipment_item WHERE id = ?', [items[0].id], lpn.data.data.id)).toBe(lpn.data.data.id);
      (await app.call('src/apis/lpn.ts#updateContainerStatus', { id: lpn.data.data.id }, { status: 'PACKED' })).holds();
    });
    await app.dispose();

    await test.step('browser: the wizard Send step sends the shipment', async () => {
      await chooseLocation(page, dc);
      await sendFromWizard(page, order.id);
      expect(await stockMovementStatus(page, order.id)).toContain('Shipped');
      expect(await committed(`SELECT c.container_number FROM shipment_item si JOIN container c ON c.id = si.container_id
                            WHERE si.shipment_id = ?`, [shipmentId], lpnNumber)).toBe(lpnNumber);
    });
    await expect.poll(() => fx.shipmentStatus(shipmentId), { timeout: 3000 }).toBe('SHIPPED');
    await expect.poll(() => fx.onHand(dc, p.id), { timeout: 30000 }).toBe(14);
  });

  knownGap('a web-wizard order (delivery type DEFAULT) can be picked into a tote and staged on the handheld',
    'PickTaskService.validateOutboundContainer requires the tote to support DeliveryTypeCode.DEFAULT.activityCode = null ("Container ... does not support null activity", 500) and pick-task.sql compares the staging lane activity to NULL, so stagingLocation is always null',
    [], async ({ fx }) => {
      const s = await mobileSite(fx, 'PPD');
      const admin = await Admin.at(s.dc);
      const order = await createOutbound(admin, s.dc, s.customer, [{ productId: s.p.id, quantity: 6 }]);
      await releaseToPicking(admin, order.id);
      await admin.dispose();
      expect(await committed('SELECT delivery_type_code FROM requisition WHERE id = ?', [order.id], 'DEFAULT')).toBe('DEFAULT');
      const app = await MobileApp.signIn(s.dc);
      const task = (await app.call('src/apis/picking.ts#getPickTasksByRequisitionApi', { facilityId: s.dc, requisitionId: order.id, 'statuses[]': ['PENDING', 'PICKING'] })).data.data[0];
      expect(task.stagingLocation?.id, 'a staging lane for the DEFAULT order').toBeTruthy();
      (await app.call('src/apis/picking.ts#patchPickTaskApi', { facilityId: s.dc, taskId: task.id }, { action: 'start', assigneeId: '1' })).holds();
      (await app.call('src/apis/picking.ts#patchPickTaskApi', { facilityId: s.dc, taskId: task.id }, { action: 'pick', outboundContainerId: s.tote.id, pickedById: '1' })).holds();
      await app.dispose();
    });

  knownGap('the handheld can drop a tote right after picking into it',
    'PickTaskService.dropToStaging reads productAvailabilityService.getAvailableItems(tote), the asynchronously refreshed product_availability: a drop within ~5 s of the pick answers 500 "Outbound container ... is empty"',
    [], async ({ fx }) => {
      const s = await mobileSite(fx, 'PPR');
      const admin = await Admin.at(s.dc);
      const order = await createOutbound(admin, s.dc, s.customer, [{ productId: s.p.id, quantity: 4 }]);
      await rows("UPDATE requisition SET delivery_type_code = 'LOCAL_DELIVERY' WHERE id = ?", [order.id]);
      await releaseToPicking(admin, order.id);
      await admin.dispose();
      const app = await MobileApp.signIn(s.dc);
      const task = (await app.call('src/apis/picking.ts#getPickTasksByRequisitionApi', { facilityId: s.dc, requisitionId: order.id, 'statuses[]': ['PENDING', 'PICKING'] })).data.data[0];
      (await app.call('src/apis/picking.ts#patchPickTaskApi', { facilityId: s.dc, taskId: task.id }, { action: 'start', assigneeId: '1' })).holds();
      (await app.call('src/apis/picking.ts#patchPickTaskApi', { facilityId: s.dc, taskId: task.id }, { action: 'pick', outboundContainerId: s.tote.id, pickedById: '1' })).holds();
      (await app.call('src/apis/picking.ts#dropPickTaskApi', { facilityId: s.dc, outboundContainerId: s.tote.id },
        { action: 'drop', stagingLocationId: s.staging.id, stagedById: '1' })).holds();
      await app.dispose();
    });
});
