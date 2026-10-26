/**
 * Receiving (inbound list, two-step partial receiving, receiving bin), containers / LPNs and packing
 *. Test Mobile DC: inbound shipments come from Main Supplier, outbound
 * ones go to it. Shipments are written in SQL the way ShipmentService leaves them (SHIPPED event).
 */
import { contractTest, expect, knownGap, test } from '../../lib/ts/test';
import { MobileApp } from '../../lib/ts/app';
import { MAIN_SUPPLIER, uid } from '../../lib/ts/env';
import { Fixtures } from '../../lib/ts/fixtures';
import { rows, scalar, committed } from '../../lib/ts/db';

type Ship = { id: string; number: string; containerId: string; itemId?: string; unpackedItemId?: string; packedItemId?: string; productId: string };

/** A SHIPPED inbound shipment from Main Supplier to Test Mobile DC, one item on a pallet. */
async function inbound(fx: Fixtures): Promise<Ship> {
  const dc = await fx.mobileDc();
  const product = await fx.product('IN');
  const lot = await fx.newLot(product.id);
  const number = `IN-${uid()}`;
  const s = await fx.shipment(MAIN_SUPPLIER, dc, { number, name: `Inbound ${number}` });
  const pallet = await fx.container(s.id, `Pallet ${number}`, '2');
  const itemId = await fx.shipmentItem(s.id, { productId: product.id, ...lot }, 10, 0, { containerId: pallet });
  await fx.markShipped(s.id, MAIN_SUPPLIER);
  return { id: s.id, number, containerId: pallet, itemId, productId: product.id };
}

/** An outbound shipment from Test Mobile DC for a PICKED requisition: one LPN holding an item, one item unpacked. */
async function outbound(fx: Fixtures): Promise<Ship> {
  const dc = await fx.mobileDc();
  const product = await fx.product('OUT');
  const lot = await fx.newLot(product.id);
  const number = `OUT-${uid()}`;
  const req = await fx.requisition(dc, MAIN_SUPPLIER, [{ productId: product.id, quantity: 8 }], { status: 'PICKED', requestNumber: number, name: `Outbound ${number}` });
  const s = await fx.shipment(dc, MAIN_SUPPLIER, { requisitionId: req.id, number, name: `Outbound ${number}` });
  const lpn = await fx.container(s.id, `LPN-${number}-00001`, '2');
  const unpackedItemId = await fx.shipmentItem(s.id, { productId: product.id, ...lot }, 5, 0);
  const packedItemId = await fx.shipmentItem(s.id, { productId: product.id, ...lot }, 3, 1, { containerId: lpn });
  return { id: s.id, number, containerId: lpn, unpackedItemId, packedItemId, productId: product.id };
}

test.describe('shipping', () => {
  let app: MobileApp;
  let dc: string;
  test.beforeEach(async ({ fx }) => { dc = await fx.mobileDc(); app = await MobileApp.signIn(dc); });
  test.afterEach(async () => { await app.dispose(); });

  const newLpn = async (s: Ship) => {
    const name = `LPN-${uid()}`;
    return app.call('src/apis/lpn.ts#saveAndUpdateLpn', {}, { name, containerNumber: name, 'containerType.id': '2', 'shipment.id': s.id });
  };

  // ------------------------------------------------------------------ receiving (inbound.ts)
  contractTest('GET /shipments?destination= lists the inbound shipment with its containers', 'src/apis/inbound.ts#fetchInboundOrderList', async ({ fx }) => {
    const s = await inbound(fx);
    (await app.call('src/apis/inbound.ts#fetchInboundOrderList', { id: dc })).rows((x) => x.id === s.id).holds();
  });

  contractTest('GET /partialReceiving/{id} answers the receipt the receive screen reads', 'src/apis/inbound.ts#fetchPartialReceiving', async ({ fx }) => {
    const s = await inbound(fx);
    (await app.call('src/apis/inbound.ts#fetchPartialReceiving', { id: s.id })).holds(['data.containers[].shipmentItems[]["binLocation']);
  });

  contractTest('POST /partialReceiving/{id}/receivingBinLocation makes the receiving bin', 'src/apis/inbound.ts#createReceivingBinLocation', async ({ fx }) => {
    const s = await inbound(fx);
    const r = await app.call('src/apis/inbound.ts#createReceivingBinLocation', { id: s.id });
    r.holds();
    expect(r.data.data.name).toContain(s.number);
  });

  contractTest('POST /partialReceiving/{id} PENDING then the echoed COMPLETED body receives the shipment', 'src/apis/inbound.ts#submitPartialReceiving', async ({ fx }) => {
    // MySQL 8 rounds fractional seconds when it stores a DATETIME, so the receipt's transaction
    // can be dated up to half a second in the future and Transaction.transactionDate's "not in the future"
    // validator refuses it (400). MariaDB truncates, so the test stays a normal one there.
    test.fixme(!process.env.E2E_KNOWN_GAPS && (process.env.E2E_DB_IMAGE ?? '').startsWith('mysql'),
      '[KNOWN-GAP] on MySQL, receiving fails about half the time (transaction dated in the future)');
    const s = await inbound(fx);
    const binId = (await app.call('src/apis/inbound.ts#createReceivingBinLocation', { id: s.id })).data.data.id;
    const receipt = (await app.call('src/apis/inbound.ts#fetchPartialReceiving', { id: s.id })).data.data;
    const container = receipt.containers.find((c: any) => c['container.id'] === s.containerId);
    const item = container.shipmentItems[0];
    const pending = {
      receiptId: '', receiptStatus: 'PENDING', shipmentId: s.id,
      containers: [{ container: { id: s.containerId }, shipmentItems: [{
        receiptItemId: '', shipmentItemId: item.shipmentItemId, product: { id: item['product.id'] }, binLocation: binId, lotNumber: item.lotNumber,
        expirationDate: item.expirationDate, recipient: item['recipient.id'], quantityReceiving: 10, cancelRemaining: false, quantityOnHand: '',
        comment: 'received on the handheld', mobile: true, lotStatusCode: '' }] }],
    };
    const step1 = await app.call('src/apis/inbound.ts#submitPartialReceiving', { id: s.id }, pending);
    const resp = step1.data.data;
    const completed = {
      isShipmentFromPurchaseOrder: resp.isShipmentFromPurchaseOrder, receiptStatus: 'COMPLETED',
      containers: resp.containers.map((c: any) => ({ ...c, container: { id: c['container.id'], name: c['container.name'], type: c['container.type'] } })),
      dateShipped: resp.dateShipped, description: resp.description, destination: { id: resp['destination.id'] }, origin: { id: resp['origin.id'] },
      receiptId: resp.receiptId, shipmentStatus: resp.shipmentStatus,
      shipment: { name: resp['shipment.name'], shipmentNumber: resp['shipment.shipmentNumber'] }, requisition: resp.requisition, recipient: resp.recipient?.id,
    };
    const step2 = await app.call('src/apis/inbound.ts#submitPartialReceiving', { id: s.id }, completed);
    step1.holds();
    expect(step2.status, step2.toString()).toBe(200);
    await expect.poll(() => fx.shipmentStatus(s.id), { timeout: 3000 }).toBe('RECEIVED');
  });

  // ------------------------------------------------------------------ containers / LPN (lpn.ts, packing.ts)
  contractTest('POST /containers/ with the create LPN body adds a pallet to the shipment', 'src/apis/lpn.ts#saveAndUpdateLpn', async ({ fx }) => {
    const s = await outbound(fx);
    const r = await newLpn(s);
    r.holds();
    expect(await committed('SELECT shipment_id FROM container WHERE id = ?', [r.data.data.id], s.id)).toBe(s.id);
  });

  contractTest('GET /generic/container/{id} answers the LPN with its items (LPN screen)', 'src/apis/lpn.ts#fetchContainer', async ({ fx }) => {
    const s = await outbound(fx);
    (await app.call('src/apis/lpn.ts#fetchContainer', { id: s.containerId })).holds();
  });

  contractTest('GET /generic/container/{id} answers the LPN with its items (packing saga, never dispatched)', 'src/apis/packing.ts#getContainerDetails', async ({ fx }) => {
    const s = await outbound(fx);
    (await app.call('src/apis/packing.ts#getContainerDetails', { id: s.containerId })).holds();
  });

  contractTest('GET /containers lists containers (inherited list action)', 'src/apis/lpn.ts#getAllContainers', async ({ fx }) => {
    const s = await outbound(fx);
    (await app.call('src/apis/lpn.ts#getAllContainers')).rows((x) => x.id === s.containerId).holds();
  });

  contractTest('GET /containers/{id}/details answers the LPN with its label URL', 'src/apis/lpn.ts#getContainerDetail', async ({ fx }) => {
    const s = await outbound(fx);
    (await app.call('src/apis/lpn.ts#getContainerDetail', { id: s.containerId })).holds();
  });

  contractTest('POST /containers/{id}/status with the LPN detail and loading bodies', 'src/apis/lpn.ts#updateContainerStatus', async ({ fx }) => {
    const s = await outbound(fx);
    (await app.call('src/apis/lpn.ts#updateContainerStatus', { id: s.containerId }, { status: 'PACKED' })).holds();
    (await app.call('src/apis/lpn.ts#updateContainerStatus', { id: s.containerId }, { status: 'LOADED', loadingLocation: 'DOCK-1' })).holds();
    expect(await committed('SELECT container_status FROM container WHERE id = ?', [s.containerId], 'LOADED')).toBe('LOADED');
  });

  contractTest('GET generic/containerType answers the container types (saga wired, never dispatched)', 'src/apis/packing.ts#getContainerType', async () => {
    (await app.call('src/apis/packing.ts#getContainerType')).holds();
  });

  // ------------------------------------------------------------------ packing (packing.ts)
  contractTest('GET /shipments?origin&shipmentStatusCode&requisitionStatus lists the shipment to pack', 'src/apis/packing.ts#getShipmentsReadyToBePacked', async ({ fx }) => {
    const s = await outbound(fx);
    (await app.call('src/apis/packing.ts#getShipmentsReadyToBePacked', { locationId: dc, shipmentStatusCode: 'PENDING' })).rows((x) => x.id === s.id).holds();
  });

  contractTest('GET /shipments/?origin= lists the outbound shipments', 'src/apis/packing.ts#getShipmentOrigin', async ({ fx }) => {
    const s = await outbound(fx);
    (await app.call('src/apis/packing.ts#getShipmentOrigin', { id: dc })).rows((x) => x.id === s.id).holds();
  });

  contractTest('GET /shipments/{id} answers the shipment the pack screen reads (all but the known gaps)', 'src/apis/packing.ts#getShipment', async ({ fx }) => {
    const s = await outbound(fx);
    (await app.call('src/apis/packing.ts#getShipment', { id: s.id })).holds([], ['data.availableContainers', 'data.containers[].shipmentItems[].quantityRemaining']);
  });

  knownGap('GET /shipments/{id} sends availableContainers and the item quantityRemaining',
    'Shipment.toJson / the ShipmentItem marshaller send neither availableContainers (mapped unguarded: TypeError on the pack screen) nor quantityRemaining (pack quantity defaults to 0)',
    'src/apis/packing.ts#getShipment', async ({ fx }) => {
      const s = await outbound(fx);
      (await app.call('src/apis/packing.ts#getShipment', { id: s.id })).holds();
    });

  contractTest('POST /shipments/{shipmentItemId} PACK moves the item into an empty LPN', 'src/apis/packing.ts#submitShipmentItems', async ({ fx }) => {
    const s = await outbound(fx);
    const lpnId = (await newLpn(s)).data.data.id;
    const r = await app.call('src/apis/packing.ts#submitShipmentItems', { id: s.unpackedItemId }, { action: 'PACK', 'container.id': lpnId, quantityToPack: 5 });
    r.holds();
    expect(await committed('SELECT container_id FROM shipment_item WHERE id = ?', [s.unpackedItemId], lpnId)).toBe(lpnId);
  });

  knownGap('POST /shipments/{shipmentItemId} PACK into an LPN holding the same lot ends up in that LPN',
    'ShipmentService.packItem merges the quantity into the SOURCE item, deletes the LPN item and never sets the container: stock stays unpacked',
    'src/apis/packing.ts#submitShipmentItems', async ({ fx }) => {
      const s = await outbound(fx);
      const r = await app.call('src/apis/packing.ts#submitShipmentItems', { id: s.unpackedItemId }, { action: 'PACK', 'container.id': s.containerId, quantityToPack: 5 });
      r.holds();
      const inLpn = await rows<{ q: number }>('SELECT quantity AS q FROM shipment_item WHERE container_id = ?', [s.containerId]);
      expect(inLpn.reduce((a, x) => a + Number(x.q), 0)).toBe(8);
    });
});
