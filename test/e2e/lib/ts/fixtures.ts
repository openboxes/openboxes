/**
 * Test data, built per test with unique names (the database is shared by the whole run and never
 * cleaned). Through OBAF's own APIs wherever one exists (locations, products, record stock, putaway
 * orders, stock movements); through SQL (lib/ts/db.ts) for the states no API can produce directly,
 * shaped the way GORM writes them.
 */
import { expect } from '@playwright/test';
import { API_URL, BIN_TYPE, DEPOT_TYPE, MAIN_SUPPLIER, MAIN_WAREHOUSE, uid } from './env';
import { MobileApp } from './app';
import { eventually, insert, newId, one, rows, scalar } from './db';

/** The Depot location type's activities (location_type 2), as the location wizard gives a new depot. */
export const DEPOT_ACTIVITIES = ['ENABLE_REQUESTOR_APPROVAL_NOTIFICATIONS', 'PLACE_REQUEST', 'SEND_STOCK', 'SUBMIT_REQUEST', 'PLACE_ORDER',
  'FULFILL_REQUEST', 'EXTERNAL', 'ENABLE_FULFILLER_APPROVAL_NOTIFICATIONS', 'RECEIVE_STOCK', 'PICK_STOCK', 'ADJUST_INVENTORY', 'PUTAWAY_STOCK',
  'MANAGE_INVENTORY'];
export const MOBILE_DC = 'TEST-MOBILE-DC';
export const SEED_CATEGORY = 'testE2eCategory000000000000000001';

export function isoDate(d: Date): string { return d.toISOString().replace(/\.\d{3}Z$/, 'Z'); }
export function days(n: number): Date { return new Date(Date.now() + n * 86400000); }

/** Fixture-side admin session (its calls are not contract exchanges). */
export class Admin {
  private constructor(readonly app: MobileApp) {}
  static async at(locationId = MAIN_WAREHOUSE): Promise<Admin> { return new Admin(await MobileApp.signIn(locationId)); }
  async dispose() { await this.app.dispose(); }

  async json(method: string, path: string, body?: unknown, ok = true): Promise<any> {
    const r = await this.app.raw(method, path, body);
    const text = await r.text();
    if (ok) expect(r.status(), `${method} ${path} -> ${r.status()} ${text.slice(0, 800)}`).toBeLessThan(300);
    try { return JSON.parse(text); } catch { return text; }
  }
  get(path: string) { return this.json('GET', path); }
  post(path: string, body?: unknown) { return this.json('POST', path, body ?? {}); }
}

const lastRecordStock = new Map<string, number>();

export class Fixtures {
  constructor(readonly admin: Admin) {}

  static async create(): Promise<Fixtures> { return new Fixtures(await Admin.at(MAIN_WAREHOUSE)); }
  async dispose() { await this.admin.dispose(); }

  // ------------------------------------------------------------------ locations
  async locationIdByNumber(number: string): Promise<string | undefined> {
    return scalar<string>('SELECT id FROM location WHERE location_number = ?', [number]);
  }

  /** A depot (Location API, as the location wizard creates one) with the depot activities plus `extra`. */
  async depot(prefix: string, extra: string[] = [], opts: { number?: string; name?: string; without?: string[] } = {}): Promise<string> {
    const number = opts.number ?? `${prefix}-${uid()}`;
    const activities = [...new Set([...DEPOT_ACTIVITIES, ...extra])].filter((a) => !(opts.without ?? []).includes(a));
    const r = await this.admin.post('/locations', {
      name: opts.name ?? number, locationNumber: number, locationType: { id: DEPOT_TYPE }, organization: { id: '1' }, active: true,
      supportedActivities: activities,
      address: { address: '1 Dock Road', city: 'Boston', stateOrProvince: 'MA', postalCode: '02110', country: 'US' },
    });
    return r.data.id;
  }

  /** The depot the handheld flows run in: REQUIRE_MOBILE_PICKING + TRACK_INTERNAL_TRANSACTIONS (seeded once). */
  async mobileDc(): Promise<string> {
    const id = await this.locationIdByNumber(MOBILE_DC);
    if (id) return id;
    return this.depot('', ['REQUIRE_MOBILE_PICKING', 'TRACK_INTERNAL_TRANSACTIONS'], { number: MOBILE_DC, name: 'Test Mobile DC' });
  }

  /** A bin under the depot. `activities` REPLACE the bin type's (give PICK_STOCK/PUTAWAY_STOCK when it must hold stock). */
  async bin(parentId: string, prefix: string, activities?: string[]): Promise<{ id: string; number: string }> {
    const number = `${prefix}-${uid()}`;
    const body: Record<string, unknown> = { name: number, locationNumber: number, locationType: { id: BIN_TYPE }, parentLocation: { id: parentId }, active: true };
    if (activities) body.supportedActivities = activities;
    const r = await this.admin.post('/locations', body);
    return { id: r.data.id, number };
  }

  // ------------------------------------------------------------------ products and stock
  /** A product with a unique code and UPC (Product API). */
  async product(prefix: string): Promise<{ id: string; productCode: string; upc: string; name: string }> {
    const code = `${prefix}-${uid()}`;
    let h = 0; for (const ch of code) h = (h * 31 + ch.charCodeAt(0)) | 0;
    const upc = String(Math.abs(h) % 1e12).padStart(12, '0');
    const name = `Test ${code}`;
    const r = await this.admin.post('/products', {
      name, productCode: code, upc, category: { id: SEED_CATEGORY }, productType: { id: 'DEFAULT' }, unitOfMeasure: 'each', active: true,
    });
    return { id: r.product.id, productCode: code, upc, name };
  }

  /**
   * Record stock (POST /api/facilities/{f}/inventory/record-stock/save, the web Record Stock page's
   * call): rows are [binId, quantity] or [binId, quantity, lotNumber, expirationDate]. Waits until
   * product availability (refreshed asynchronously in production) shows the new quantity.
   */
  async stock(facilityId: string, productId: string, rows_: [string, number, string?, Date?][]): Promise<void> {
    const inv = await scalar<string>('SELECT inventory_id FROM location WHERE id = ?', [facilityId]);
    // record stock writes its transactions a second or two after transactionDate, a transaction dated
    // in the future is invalid, and two record stocks of one product in the same second collide
    // ("A transaction already exists at time ..."): date it 3 s back, at least 3 s after the last one
    const last = lastRecordStock.get(productId) ?? 0;
    const wait = last + 3000 - (Date.now() - 3000);
    if (wait > 0) await new Promise((res) => setTimeout(res, wait));
    const when = Date.now() - 3000;
    lastRecordStock.set(productId, when);
    const r = await this.admin.post(`/facilities/${facilityId}/inventory/record-stock/save`, {
      product: { id: productId }, inventory: { id: inv }, transactionDate: isoDate(new Date(when)), comment: 'e2e fixture',
      recordInventoryRows: rows_.map(([bin, q, lot, exp]) => ({
        binLocation: { id: bin }, lotNumber: lot ?? null, expirationDate: exp ? isoDate(exp) : null, oldQuantity: 0, newQuantity: q,
      })),
    });
    expect(r.data?.errors?.errors ?? [], 'record stock errors').toEqual([]);
    await this.awaitOnHand(facilityId, productId, rows_.reduce((s, x) => s + x[1], 0), 'at least');
    // the facility total can already be high enough from earlier stock: wait for each recorded bin and lot too
    for (const [bin, q, lot] of rows_) {
      const sql = `SELECT COALESCE(SUM(pa.quantity_on_hand), 0) FROM product_availability pa
          LEFT JOIN inventory_item ii ON ii.id = pa.inventory_item_id
         WHERE pa.location_id = ? AND pa.product_id = ? AND pa.bin_location_id = ? AND ii.lot_number <=> ?`;
      const ok = await eventually(async () => Number(await scalar(sql, [facilityId, productId, bin, lot ?? null])) >= q, 30000);
      expect(ok, `product availability of ${productId} in bin ${bin} lot ${lot ?? '(none)'} never reached at least ${q}`).toBeTruthy();
    }
  }

  async onHand(facilityId: string, productId: string): Promise<number> {
    return Number(await scalar('SELECT COALESCE(SUM(quantity_on_hand), 0) FROM product_availability WHERE location_id = ? AND product_id = ?', [facilityId, productId]));
  }

  async awaitOnHand(facilityId: string, productId: string, quantity: number, mode: 'exactly' | 'at least' = 'exactly'): Promise<void> {
    const ok = await eventually(async () => {
      const q = await this.onHand(facilityId, productId);
      return mode === 'exactly' ? q === quantity : q >= quantity;
    }, 30000);
    expect(ok, `product availability of ${productId} at ${facilityId} never reached ${mode} ${quantity} (now ${await this.onHand(facilityId, productId)})`).toBeTruthy();
  }

  /** On hand per bin location number for one product at a facility (product_availability). */
  async onHandByBin(facilityId: string, productId: string): Promise<Record<string, number>> {
    const r = await rows<{ bin: string | null; q: number }>(
      `SELECT bin.location_number AS bin, SUM(pa.quantity_on_hand) AS q FROM product_availability pa
         LEFT JOIN location bin ON bin.id = pa.bin_location_id
        WHERE pa.location_id = ? AND pa.product_id = ? GROUP BY bin.location_number HAVING SUM(pa.quantity_on_hand) <> 0`, [facilityId, productId]);
    return Object.fromEntries(r.map((x) => [x.bin ?? '(no bin)', Number(x.q)]));
  }

  async lotId(productId: string, lotNumber: string): Promise<string> {
    const id = await scalar<string>('SELECT id FROM inventory_item WHERE product_id = ? AND lot_number = ?', [productId, lotNumber]);
    expect(id, `lot ${lotNumber}`).toBeTruthy();
    return id!;
  }

  async newLot(productId: string, lotNumber = `LOT-${uid()}`, expiry = days(365)): Promise<{ id: string; lotNumber: string; expiry: Date }> {
    const id = await insert('inventory_item', { product_id: productId, lot_number: lotNumber, expiration_date: expiry });
    return { id, lotNumber, expiry };
  }

  // ------------------------------------------------------------------ orders (SQL, GORM-shaped)
  /** A requisition (outbound order) from `originId`; lines: product, quantity (+ backordered). Returns ids. */
  async requisition(originId: string, destinationId: string, lines: { productId: string; quantity: number; quantityBackordered?: number }[],
    props: { status?: string; deliveryTypeCode?: string; autoAllocationRequested?: boolean; name?: string; requestNumber?: string } = {}) {
    const n = uid();
    const requestNumber = props.requestNumber ?? `LE${n}`;
    const reqId = await insert('requisition', {
      name: props.name ?? `Test e2e ${n}`, request_number: requestNumber, origin_id: originId, destination_id: destinationId,
      requested_by_id: '1', date_requested: new Date(), requested_delivery_date: days(1), status: props.status ?? 'CREATED', type: 'DEFAULT',
      delivery_type_code: props.deliveryTypeCode ?? 'LOCAL_DELIVERY', auto_allocation_requested: props.autoAllocationRequested ? 1 : 0,
      is_template: 0,
    });
    const itemIds: string[] = [];
    for (const [i, l] of lines.entries()) {
      itemIds.push(await insert('requisition_item', {
        requisition_id: reqId, product_id: l.productId, quantity: l.quantity, quantity_approved: l.quantity, substitutable: 0, order_index: i,
        requisition_items_idx: i, quantity_backordered: l.quantityBackordered ?? null,
        backordered_reason_code: l.quantityBackordered ? 'BACKORDER' : null,
      }));
    }
    return { id: reqId, requestNumber, itemIds };
  }

  /** A picklist on the requisition; items: [requisitionItemId, inventoryItemId, binId, quantity]. Returns picklist + item ids. */
  async picklist(requisitionId: string, items: [string, string, string, number][]) {
    const picklistId = await insert('picklist', { name: `Picklist ${requisitionId}`, requisition_id: requisitionId });
    const itemIds: string[] = [];
    for (const [i, [ri, lot, bin, q]] of items.entries()) {
      itemIds.push(await insert('picklist_item', {
        picklist_id: picklistId, requisition_item_id: ri, inventory_item_id: lot, bin_location_id: bin, quantity: q, quantity_picked: 0,
        sort_order: i, picklist_items_idx: i,
      }));
    }
    return { picklistId, itemIds };
  }

  /** A shipment row (PENDING); optional requisition. */
  async shipment(originId: string, destinationId: string, props: { requisitionId?: string; number?: string; name?: string } = {}) {
    const number = props.number ?? `LS${uid()}`;
    const id = await insert('shipment', {
      name: props.name ?? number, shipment_number: number, origin_id: originId, destination_id: destinationId,
      expected_shipping_date: new Date(), expected_delivery_date: days(2), shipment_type_id: '5', requisition_id: props.requisitionId ?? null,
      current_status: 'PENDING',
    });
    return { id, number };
  }

  async container(shipmentId: string, name: string, typeId = '2', sortOrder = 0): Promise<string> {
    return insert('container', { shipment_id: shipmentId, name, container_number: name, container_type_id: typeId, sort_order: sortOrder });
  }

  async shipmentItem(shipmentId: string, lot: { productId: string; id: string; lotNumber: string; expiry?: Date }, quantity: number, idx: number,
    extra: { containerId?: string; backorderReference?: string; requisitionItemId?: string } = {}): Promise<string> {
    return insert('shipment_item', {
      shipment_id: shipmentId, product_id: lot.productId, inventory_item_id: lot.id, lot_number: lot.lotNumber, expiration_date: lot.expiry ?? null,
      quantity, container_id: extra.containerId ?? null, shipment_items_idx: idx, sort_order: idx,
      backorder_reference: extra.backorderReference ?? null, requisition_item_id: extra.requisitionItemId ?? null,
    });
  }

  /** Marks a shipment SHIPPED the way ShipmentService.createShipmentEvent does (event + shipment_event + current status). */
  async markShipped(shipmentId: string, originId: string): Promise<void> {
    const eventId = await insert('event', { event_date: new Date(Date.now() - 60000), event_type_id: '2', event_location_id: originId });
    await insert('shipment_event', { shipment_events_id: shipmentId, event_id: eventId });
    await rows('UPDATE shipment SET current_status = ?, current_event_id = ? WHERE id = ?', ['SHIPPED', eventId, shipmentId]);
  }

  /** An event type the packing / loading actions look up by code (no migration seeds them). */
  async eventTypeId(code: string): Promise<string | undefined> {
    return scalar<string>('SELECT id FROM event_type WHERE event_code = ?', [code]);
  }

  async requisitionStatus(id: string): Promise<string> { return (await scalar<string>('SELECT status FROM requisition WHERE id = ?', [id]))!; }
  async shipmentStatus(id: string): Promise<string> { return (await scalar<string>('SELECT current_status FROM shipment WHERE id = ?', [id]))!; }

  async documentId(number: string): Promise<string> {
    const id = await scalar<string>('SELECT id FROM document WHERE document_number = ?', [number]);
    expect(id, `seeded document ${number}`).toBeTruthy();
    return id!;
  }

  /** Grants (true) or revokes (false) a role type to admin (user 1). */
  async grantAdminRole(roleType: string, on: boolean): Promise<void> {
    if (on) {
      await rows(`INSERT INTO user_role (user_id, role_id) SELECT '1', id FROM role WHERE role_type = ?
                  AND NOT EXISTS (SELECT 1 FROM user_role ur JOIN role ro ON ro.id = ur.role_id WHERE ur.user_id = '1' AND ro.role_type = ?)`, [roleType, roleType]);
    } else {
      await rows(`DELETE FROM user_role WHERE user_id = '1' AND role_id IN (SELECT id FROM role WHERE role_type = ?)`, [roleType]);
    }
  }
}

export { MAIN_SUPPLIER, MAIN_WAREHOUSE, newId, one };

// ------------------------------------------------------------------ small product helpers
declare module './fixtures' {
  interface Fixtures {
    productUpc(productId: string): Promise<string | undefined>;
    productImage(productId: string): Promise<string>;
  }
}
Fixtures.prototype.productUpc = async function (productId: string) {
  return scalar<string>('SELECT upc FROM product WHERE id = ?', [productId]);
};
/** A product image document (product.documents), as an upload on the product page leaves it. */
Fixtures.prototype.productImage = async function (productId: string) {
  const png = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==', 'base64');
  const id = await insert('document', { name: 'photo', filename: 'photo.png', content_type: 'image/png', file_contents: png });
  await rows('INSERT INTO product_document (product_id, document_id) VALUES (?, ?)', [productId, id]);
  return id;
};

declare module './fixtures' {
  interface Fixtures {
    inventoryLevel(facilityId: string, productId: string, props: { internalLocationId?: string; preferredBinLocationId?: string }): Promise<string>;
    orderStatus(orderId: string): Promise<string | undefined>;
  }
}
/** An InventoryLevel (stock settings of a product at a facility: bin level, preferred bin). */
Fixtures.prototype.inventoryLevel = async function (facilityId, productId, props) {
  const inv = await scalar<string>('SELECT inventory_id FROM location WHERE id = ?', [facilityId]);
  return insert('inventory_level', {
    inventory_id: inv, product_id: productId, internal_location_id: props.internalLocationId ?? null,
    preferred_bin_location_id: props.preferredBinLocationId ?? null,
  });
};
Fixtures.prototype.orderStatus = async function (orderId) {
  return scalar<string>('SELECT status FROM `order` WHERE id = ?', [orderId]);
};
