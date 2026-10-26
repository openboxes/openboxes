/**
 * Products: catalogue lists and searches, barcode and global scan lookups, product details, UPC
 * update, stock adjustment, labels. Label calls reach only the
 * label stub on the sealed network (printer socket + labelary render).
 */
import { contractTest, expect, knownGap, test } from '../../lib/ts/test';
import { MobileApp } from '../../lib/ts/app';
import { allBut } from '../../lib/ts/contract';
import { MAIN_SUPPLIER, MAIN_WAREHOUSE, uid } from '../../lib/ts/env';
import { days, Fixtures, SEED_CATEGORY } from '../../lib/ts/fixtures';
import { eventually } from '../../lib/ts/db';
import { labelStub } from '../../lib/ts/labels';

const SCAN = 'src/apis/products.ts#searchBarcode';

/** An LPN on an inbound shipment (Main Supplier -> Main Warehouse). */
async function lpn(fx: Fixtures): Promise<{ id: string; number: string }> {
  const number = `CN-${uid()}`;
  const s = await fx.shipment(MAIN_SUPPLIER, MAIN_WAREHOUSE, { number: `S-${number}` });
  return { id: await fx.container(s.id, number, '1'), number };
}

test.describe('products', () => {
  let app: MobileApp;
  let bin: { id: string; number: string };
  let p: { id: string; productCode: string; upc: string; name: string };
  let lot: { id: string; lotNumber: string };

  test.beforeEach(async ({ fx }) => {
    app = await MobileApp.signIn(MAIN_WAREHOUSE);
    bin = await fx.bin(MAIN_WAREHOUSE, 'PR-BIN', ['PICK_STOCK', 'PUTAWAY_STOCK']);
    p = await fx.product('PR');
    const lotNumber = `LOT-${uid()}`;
    await fx.stock(MAIN_WAREHOUSE, p.id, [[bin.id, 9, lotNumber, days(365)]]);
    lot = { id: await fx.lotId(p.id, lotNumber), lotNumber };
  });
  test.afterEach(async () => { await app.dispose(); });

  // ------------------------------------------------------------------ lists and searches
  contractTest('GET /generic/product?max&offset pages the catalogue', 'src/apis/products.ts#getProducts', async () => {
    const r = await app.call('src/apis/products.ts#getProducts', { 'pagination.max': 5, 'pagination.offset': 0 });
    r.holds();
    expect(r.data.data.length).toBeLessThanOrEqual(5);
  });

  contractTest('GET /generic/category lists the categories for the picker', 'src/data/product/category/GetProductCategories.ts#getProductCategories', async () => {
    (await app.call('src/data/product/category/GetProductCategories.ts#getProductCategories')).holds();
  });

  contractTest('POST /mobile/products/search {value} finds the product by code', 'src/apis/products.ts#searchProductGlobally', async () => {
    const r = await app.call('src/apis/products.ts#searchProductGlobally', {}, { value: p.productCode });
    r.holds();
    expect(r.data.data.map((x: any) => x.id)).toContain(p.id);
  });

  contractTest('POST /generic/product/search by productCode like (search header, unreachable in the UI)', 'src/apis/products.ts#searchProductByCode', async () => {
    const r = await app.call('src/apis/products.ts#searchProductByCode', {},
      { searchAttributes: [{ property: 'productCode', operator: 'like', value: `${p.productCode}%` }] });
    r.holds();
    expect(r.data.data.map((x: any) => x.id)).toContain(p.id);
  });

  contractTest('POST /generic/product/search by category answers (today: the {data} envelope)', 'src/apis/products.ts#searchProductsByCategory', async () => {
    const r = await app.call('src/apis/products.ts#searchProductsByCategory', {},
      { searchAttributes: [{ property: 'category.id', operator: 'eq', value: SEED_CATEGORY }] });
    expect(r.status).toBe(200);
    expect(r.data.data.map((x: any) => x.id)).toContain(p.id);
  }, { characterise: '{data:[...]} where the caller reads a bare array' });

  knownGap('POST /generic/product/search by category answers a bare array',
    '(dead path) GenericApiController.search answers {data:[...]}, searchProductsByCategory reads a bare array',
    'src/apis/products.ts#searchProductsByCategory', async () => {
      (await app.call('src/apis/products.ts#searchProductsByCategory', {},
        { searchAttributes: [{ property: 'category.id', operator: 'eq', value: SEED_CATEGORY }] })).holds();
    });

  contractTest('POST /mobile/products/search by name like answers 200 (today: always empty)', 'src/apis/products.ts#searchProductsByName', async () => {
    const r = await app.call('src/apis/products.ts#searchProductsByName', {},
      { searchAttributes: [{ property: 'name', operator: 'like', value: `${p.name}%` }] });
    expect(r.status).toBe(200);
  }, { characterise: 'always {data:[]}' });

  knownGap('POST /mobile/products/search by name like finds the product',
    'MobileProductApiController.search reads only `value`, so searchProductsByName always gets {data:[]} (and expects a bare array)',
    'src/apis/products.ts#searchProductsByName', async () => {
      const r = await app.call('src/apis/products.ts#searchProductsByName', {},
        { searchAttributes: [{ property: 'name', operator: 'like', value: `${p.name}%` }] });
      r.holds();
      expect((r.json as any[]).map((x) => x.id)).toContain(p.id);
    });

  // ------------------------------------------------------------------ barcode and scan lookups
  contractTest('GET /barcodes?id={upc} answers the product', 'src/apis/products.ts#getProductByBarcode', async () => {
    const r = await app.call('src/apis/products.ts#getProductByBarcode', { barcode: p.upc });
    r.holds();
    expect(r.data.data.id).toBe(p.id);
  });

  contractTest('GET /barcodes/{productCode} answers the product', 'src/apis/createTransfer.ts#lookupBarcodeProduct', async () => {
    const r = await app.call('src/apis/createTransfer.ts#lookupBarcodeProduct', { code: p.productCode });
    r.holds();
    expect(r.data.data.id).toBe(p.id);
  });

  knownGap('an unknown barcode answers 404',
    'ApiController.barcodes throws ApiException for an unknown code: 500, not 404 (the app treats the 500 as not found today)',
    ['src/apis/products.ts#getProductByBarcode', 'src/apis/createTransfer.ts#lookupBarcodeProduct'], async () => {
      expect((await app.call('src/apis/products.ts#getProductByBarcode', { barcode: `NOPE-${uid()}` })).status).toBe(404);
      expect((await app.call('src/apis/createTransfer.ts#lookupBarcodeProduct', { code: `NOPE-${uid()}` })).status).toBe(404);
    });

  contractTest('GET /globalSearch/{code} resolves a product, a lot, a bin and an LPN', SCAN, async ({ fx }) => {
    const bare = await fx.newLot(p.id, `LOT0-${uid()}`);
    const container = await lpn(fx);
    const product = await app.call(SCAN, { id: p.productCode });
    const inventoryItem = await app.call(SCAN, { id: bare.lotNumber });
    const location = await app.call(SCAN, { id: bin.number });
    const c = await app.call(SCAN, { id: container.number });
    expect(product.data.type).toBe('Product');
    product.holds([], allBut(SCAN, ['type', 'data', 'data.id']));
    expect(inventoryItem.data.type).toBe('InventoryItem');
    inventoryItem.holds([], allBut(SCAN, ['type', 'data', 'data.product.id']));
    expect(location.data.type).toBe('Location');
    location.holds([], allBut(SCAN, ['type', 'data']));
    expect(c.data.type).toBe('Container');
    c.holds([], allBut(SCAN, ['type', 'data', 'data.id', 'data.shipmentNumber']));
  });

  knownGap('GET /globalSearch/{lot} of a lot stocked in one bin answers the available item',
    'SearchService.findInventoryItem calls AuthService.currentLocation.get() (GORM get() with no id = null), so a stocked lot answers InventoryItem and the AvailableItem branch of the Scan screen is unreachable',
    SCAN, async () => {
      const available = await app.call(SCAN, { id: lot.lotNumber });
      expect(available.data.type).toBe('AvailableItem');
      available.holds([], allBut(SCAN, ['type', 'data', 'data.binLocation.name', 'data.lotNumber', 'data.quantityOnHand', 'data.quantityAvailable',
        'data.product.productCode', 'data.product.name', 'data.inventoryItem.product.defaultImageUrl']));
    });

  knownGap('GET /globalSearch/{unknown} answers 404',
    'ApiController.globalSearch throws IllegalStateException when nothing matches: 500, not 404',
    SCAN, async () => {
      expect((await app.call(SCAN, { id: `NOPE-${uid()}` })).status).toBe(404);
    });

  // ------------------------------------------------------------------ details, identifiers, adjustments
  contractTest('GET /mobile/products/{id}/details answers stock, bins and the label URL', 'src/apis/products.ts#getProductById', async () => {
    const r = await app.call('src/apis/products.ts#getProductById', { id: p.id });
    r.holds();
    expect(r.data.data.defaultBarcodeLabelUrl.id).toBeTruthy();
    expect(r.data.data.quantityOnHand).toBe(9);
  });

  contractTest('GET defaultBarcodeLabelUrl renders the label image through the labelary stub (no internet)', 'src/apis/products.ts#getProductById', async () => {
    const u: string = (await app.call('src/apis/products.ts#getProductById', { id: p.id })).data.data.defaultBarcodeLabelUrl.url;
    const path = u.substring(u.indexOf('/openboxes/api') + '/openboxes/api'.length);
    const image = await app.raw('GET', path);
    expect(image.status()).toBe(200);
    expect(image.headers()['content-type']).toMatch(/^image\/png/);
    expect((await labelStub()).rendered.some((z) => z.includes(p.productCode))).toBe(true);
  });

  knownGap('GET /mobile/products/{id}/details sends images with a url',
    'MobileProductApiController.details renders images as {id,name,contentType,uri}; the app types {url}',
    'src/apis/products.ts#getProductById', async ({ fx }) => {
      const docId = await fx.productImage(p.id);
      expect(docId).toBeTruthy();
      const r = await app.call('src/apis/products.ts#getProductById', { id: p.id });
      expect(r.status).toBe(200);
      expect(typeof r.data.data.images[0].url).toBe('string');
    });

  contractTest('PUT /mobile/products/{id}/identifiers sets the UPC for a product manager', 'src/apis/products.ts#updateProductIdentifier', async ({ fx }) => {
    await fx.grantAdminRole('ROLE_PRODUCT_MANAGER', true);
    try {
      const upc = String(Date.now()).slice(-12).padStart(12, '7');
      const r = await app.call('src/apis/products.ts#updateProductIdentifier', { id: p.id, type: 'upc', value: upc }, { identifier: { type: 'upc', value: upc } });
      r.holds();
      await expect.poll(() => fx.productUpc(p.id), { timeout: 3000 }).toBe(upc);
    } finally {
      await fx.grantAdminRole('ROLE_PRODUCT_MANAGER', false);
    }
  });

  contractTest('POST /stockAdjustments with the adjust stock body records the new quantity', 'src/apis/products.ts#stockAdjustments', async () => {
    // step past the record-stock second (InventoryService.adjustStock's same-second check is wiped by validate())
    await new Promise((r) => setTimeout(r, 1500));
    const r = await app.call('src/apis/products.ts#stockAdjustments', {},
      { 'location.id': MAIN_WAREHOUSE, 'product.id': p.id, inventoryItem: lot.id, binLocation: bin.id, currentQuantity: 9,
        reasonCode: 'CORRECTION', newQuantity: 6, comment: 'counted on the handheld' });
    r.holds([], ['data[']);
    // production refreshes availability asynchronously: read what the app reads until it settles
    const onHand = await eventually(async () => {
      const q = (await app.call('src/apis/products.ts#getProductById', { id: p.id })).data.data.quantityOnHand;
      return q === 6 ? q : 0;
    }, 30000, 500);
    expect(onHand).toBe(6);
  });

  knownGap('POST /stockAdjustments answers the adjusted row the screen reads',
    'StockAdjustmentApiController.create renders {data: <AdjustStockCommand>}; the AdjustStock screen reads data[0].quantityAvailable/quantityAdjusted',
    'src/apis/products.ts#stockAdjustments', async () => {
      await new Promise((r) => setTimeout(r, 1500));
      (await app.call('src/apis/products.ts#stockAdjustments', {},
        { 'location.id': MAIN_WAREHOUSE, 'product.id': p.id, inventoryItem: lot.id, binLocation: bin.id, currentQuantity: 9,
          reasonCode: 'CORRECTION', newQuantity: 5, comment: 'counted on the handheld' })).holds();
    });

  // ------------------------------------------------------------------ labels
  contractTest('POST /{products|internalLocations|containers}/{id}/labels/{documentId} prints ZPL to the printer', 'src/apis/products.ts#printLabel', async ({ fx }) => {
    const container = await lpn(fx);
    const docs = {
      product: await fx.documentId('barcodeLabel:product'),
      location: await fx.documentId('barcodeLabel:internalLocation'),
      container: await fx.documentId('barcodeLabel:container'),
    };
    const results = [
      await app.call('src/apis/products.ts#printLabel', { 'data.type': 'products', 'data.productId': p.id, 'data.barcodeId': docs.product }),
      await app.call('src/apis/products.ts#printLabel', { 'data.type': 'internalLocations', 'data.productId': bin.id, 'data.barcodeId': docs.location }),
      await app.call('src/apis/products.ts#printLabel', { 'data.type': 'containers', 'data.productId': container.id, 'data.barcodeId': docs.container }),
    ];
    for (const c of results) {
      c.holds();
      expect(c.data?.errorCode, c.toString()).toBeFalsy();
    }
    const printed = await eventually(async () => {
      const got = (await labelStub()).printed;
      return [p.productCode, bin.number, container.number].every((s) => got.some((z) => z.includes(s))) ? got : null;
    }, 10000, 200);
    expect(printed, 'the printer stub received the three labels').toBeTruthy();
  });

  knownGap('a label that cannot be printed answers an error status',
    'printLabel catches the failure and answers HTTP 200 {errorCode:500} (unknown document, the app\'s literal "undefined" barcodeId): a failed print looks printed',
    'src/apis/products.ts#printLabel', async () => {
      const r = await app.call('src/apis/products.ts#printLabel', { 'data.type': 'products', 'data.productId': p.id, 'data.barcodeId': 'undefined' });
      expect(r.status).toBeGreaterThanOrEqual(400);
    });

  contractTest('a label that cannot be printed today: HTTP 200 with {errorCode} (characterised)', 'src/apis/products.ts#printLabel', async () => {
    const r = await app.call('src/apis/products.ts#printLabel', { 'data.type': 'products', 'data.productId': p.id, 'data.barcodeId': 'undefined' });
    expect(r.status).toBe(200);
    expect(r.data?.errorCode).toBe(500);
  }, { characterise: 'a failed print answers 200 {errorCode:500}' });
});
