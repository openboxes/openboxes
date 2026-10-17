/**
 * OBAF workflows driven through the same API calls the web app's React wizards make
 * (src/js/components/stock-movement-wizard/**): create the movement, add items, send it.
 */
import { expect } from '@playwright/test';
import { Admin } from './fixtures';
import { scalar } from './db';

export function mdy(d: Date): string {
  return `${String(d.getUTCMonth() + 1).padStart(2, '0')}/${String(d.getUTCDate()).padStart(2, '0')}/${d.getUTCFullYear()}`;
}

export type Line = { productId: string; quantity: number; lotNumber?: string; expiry?: Date };

/**
 * An inbound stock movement (Inbound wizard: CreateStockMovement -> AddItemsPage -> SendMovementPage),
 * sent: the shipment is SHIPPED (and Shipment.afterUpdate publishes the automatic receipt event).
 * `admin` must be signed in at the destination.
 */
export async function sendInbound(admin: Admin, originId: string, destinationId: string, lines: Line[], description = 'e2e inbound') {
  const created = await admin.post('/stockMovements', {
    name: '', description, dateRequested: mdy(new Date()), origin: { id: originId }, destination: { id: destinationId },
    requestedBy: { id: '1' }, stocklist: { id: '' },
  });
  const id: string = created.data.id;
  await admin.post(`/stockMovements/${id}/updateItems`, {
    id,
    lineItems: lines.map((l, i) => ({
      product: { id: l.productId }, quantityRequested: l.quantity, palletName: '', boxName: '', lotNumber: l.lotNumber ?? '',
      expirationDate: l.expiry ? mdy(l.expiry) : '', recipient: { id: '' }, sortOrder: (i + 1) * 100,
    })),
  });
  // AddItemsPage "Next": status CHECKING (this is what creates the shipment items)
  await admin.post(`/stockMovements/${id}/status`, { status: 'CHECKING' });
  await admin.post(`/stockMovements/${id}/updateShipment`, {
    dateShipped: mdy(new Date()), shipmentType: '5', trackingNumber: '', driverName: '', comments: '',
    expectedDeliveryDate: mdy(new Date(Date.now() + 86400000)),
  });
  await admin.post(`/stockMovements/${id}/status`, { status: 'DISPATCHED' });
  const sm = (await admin.get(`/stockMovements/${id}`)).data;
  const shipmentId = (await scalar<string>('SELECT id FROM shipment WHERE requisition_id = ?', [id]))!;
  expect(shipmentId, `shipment of stock movement ${id}: ${JSON.stringify(sm).slice(0, 500)}`).toBeTruthy();
  return { stockMovementId: id, shipmentId, identifier: sm.identifier as string };
}

/**
 * An outbound stock movement as the Outbound wizard makes it (CreateStockMovement -> AddItemsPage
 * "Next" = REQUESTED), not yet released to picking. Returns the stock movement (= requisition) id.
 */
export async function createOutbound(admin: Admin, originId: string, destinationId: string, lines: Line[], description = 'e2e outbound') {
  const created = await admin.post('/stockMovements', {
    name: '', description, dateRequested: mdy(new Date()), origin: { id: originId }, destination: { id: destinationId },
    requestedBy: { id: '1' }, stocklist: { id: '' }, requestType: 'STOCK',
  });
  const id: string = created.data.id;
  await admin.post(`/stockMovements/${id}/updateItems`, {
    id, lineItems: lines.map((l, i) => ({ product: { id: l.productId }, quantityRequested: l.quantity, sortOrder: (i + 1) * 100 })),
  });
  await admin.post(`/stockMovements/${id}/status`, { status: 'REQUESTED' });
  return { id, identifier: (await admin.get(`/stockMovements/${id}`)).data.identifier as string };
}

/** EditPage "Next": PICKING with createPicklist (the allocation / autopick). */
export async function releaseToPicking(admin: Admin, id: string) {
  await admin.post(`/stockMovements/${id}/status`, { status: 'PICKING', createPicklist: 'true' });
}
