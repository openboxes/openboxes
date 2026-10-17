/**
 * Stock transfers: Create Transfer flow, Transfers list/detail, the legacy Transfer screen. Main Warehouse; each test makes its own product and two bins.
 */
import { contractTest, expect, knownGap, test } from '../../lib/ts/test';
import { MobileApp } from '../../lib/ts/app';
import { expandFlatKeys } from '../../lib/ts/contract';
import { MAIN_WAREHOUSE } from '../../lib/ts/env';
import { scalar } from '../../lib/ts/db';

test.describe('stock transfers', () => {
  let app: MobileApp;
  let p: { id: string };
  let binA: { id: string; number: string };
  let binB: { id: string; number: string };

  test.beforeEach(async ({ fx }) => {
    app = await MobileApp.signIn(MAIN_WAREHOUSE);
    p = await fx.product('ST');
    binA = await fx.bin(MAIN_WAREHOUSE, 'ST-A');
    binB = await fx.bin(MAIN_WAREHOUSE, 'ST-B');
    await fx.stock(MAIN_WAREHOUSE, p.id, [[binA.id, 10]]);
  });
  test.afterEach(async () => { await app.dispose(); });

  async function candidateRow() {
    const c = await app.call('src/apis/createTransfer.ts#getStockTransferCandidates', { facilityId: MAIN_WAREHOUSE });
    const row = c.data.data.find((x: any) => x['product.id'] === p.id && x['originBinLocation.id'] === binA.id);
    expect(row, `candidate row for ${p.id} in ${binA.number}`).toBeTruthy();
    return row;
  }
  /** The Create Transfer flow's body (CreateTransferDestinationScreen), PENDING. */
  const pendingBody = (row: any, qty = 4) => ({
    stockTransferItems: [{
      id: null, productAvailabilityId: row.productAvailabilityId, product: { id: row['product.id'] }, inventoryItem: { id: row['inventoryItem.id'] },
      originBinLocation: { id: binA.id }, destinationBinLocation: { id: binB.id }, quantity: qty, quantityOnHand: row.quantityOnHand,
      quantityNotPicked: row.quantityNotPicked, status: 'PENDING', splitItems: [], sortOrder: 0,
    }],
  });
  async function createPending(): Promise<string> {
    const r = await app.call('src/apis/createTransfer.ts#postStockTransfer', {}, pendingBody(await candidateRow()));
    r.holds();
    return r.data.data.id;
  }
  /** The app's completion body: GET /stockTransfers/{id}.data through parseResponse, plus status. */
  async function completionBody(id: string): Promise<any> {
    const get = await app.call('src/apis/createTransfer.ts#getStockTransferById', { id });
    get.holds();
    return { ...(expandFlatKeys(get.data.data) as object), status: 'COMPLETED' };
  }
  const ordersNumbered = async (n: string) => Number(await scalar('SELECT COUNT(*) FROM `order` WHERE order_number = ?', [n]));

  contractTest('GET /stockTransfers/candidates lists the stocked bin with the fields the item list reads', 'src/apis/createTransfer.ts#getStockTransferCandidates', async () => {
    const r = await app.call('src/apis/createTransfer.ts#getStockTransferCandidates', { facilityId: MAIN_WAREHOUSE });
    r.rows((x) => x['product.id'] === p.id).holds();
    expect(r.data.data.some((x: any) => x['product.id'] === p.id && x['originBinLocation.id'] === binA.id && x.quantityOnHand === 10)).toBe(true);
  });

  contractTest('POST /stockTransfers with the Create Transfer body makes a PENDING transfer', 'src/apis/createTransfer.ts#postStockTransfer', async () => {
    const r = await app.call('src/apis/createTransfer.ts#postStockTransfer', {}, pendingBody(await candidateRow()));
    r.holds();
    expect(r.data.data.status).toBe('PENDING');
  });

  contractTest('GET /stockTransfers/{id} answers the transfer (Create Transfer detail)', 'src/apis/createTransfer.ts#getStockTransferById', async () => {
    const id = await createPending();
    (await app.call('src/apis/createTransfer.ts#getStockTransferById', { id })).holds();
  });

  contractTest('GET /stockTransfers/{id} answers the transfer (Transfers summary)', 'src/apis/transfers.ts#fetchStockTransferSummary', async () => {
    const id = await createPending();
    const summary = await app.call('src/apis/transfers.ts#fetchStockTransferSummary', { id });
    summary.holds();
    expect(summary.data.data.id).toBe(id);
    expect(summary.data.data.stockTransferItems.length).toBe(1);
  });

  contractTest('PUT /stockTransfers/{id} with the parsed GET body completes the transfer in place', 'src/apis/createTransfer.ts#putStockTransfer', async () => {
    const id = await createPending();
    const body = await completionBody(id);
    const r = await app.call('src/apis/createTransfer.ts#putStockTransfer', { id }, body);
    const after = await app.call('src/apis/transfers.ts#fetchStockTransferSummary', { id });
    r.holds();
    expect(after.data.data.status).toBe('COMPLETED');
    expect(await ordersNumbered(body.stockTransferNumber)).toBe(1);
  });

  contractTest('POST /stockTransfers/{id} with the parsed summary body completes the transfer in place', 'src/apis/transfers.ts#completeStockTransfer', async () => {
    const id = await createPending();
    const body = { ...(await completionBody(id)), id };
    const r = await app.call('src/apis/transfers.ts#completeStockTransfer', { 'data.id': id }, body);
    const after = await app.call('src/apis/transfers.ts#fetchStockTransferSummary', { id });
    r.holds();
    expect(r.data.data.id).toBe(id);
    expect(after.data.data.status).toBe('COMPLETED');
    expect(await ordersNumbered(body.stockTransferNumber)).toBe(1);
  });

  contractTest('GET /stockTransfers/?location= lists the facility transfers with the list row fields', 'src/apis/transfers.ts#getStockTransfers', async () => {
    const id = await createPending();
    const r = await app.call('src/apis/transfers.ts#getStockTransfers', { data: { params: { location: MAIN_WAREHOUSE } } });
    r.rows((x) => x.id === id).holds();
  });

  contractTest('POST /stockTransfers through the undispatched stockTransfers action accepts a pending body', 'src/apis/transfers.ts#stockTransfers', async () => {
    (await app.call('src/apis/transfers.ts#stockTransfers', {}, pendingBody(await candidateRow(), 2))).holds();
  });

  const legacyBody = async () => {
    const row = await candidateRow();
    return {
      status: 'COMPLETED', stockTransferNumber: '', description: '', 'origin.id': MAIN_WAREHOUSE, 'destination.id': MAIN_WAREHOUSE,
      stockTransferItems: [{ product: { id: p.id }, inventoryItem: { id: row['inventoryItem.id'] }, location: { id: MAIN_WAREHOUSE },
        originBinLocation: { id: binA.id }, destinationBinLocation: { id: binB.id }, quantity: 3 }],
    };
  };

  contractTest('POST /stockTransfers with the legacy COMPLETED body today: 500 (characterised)', 'src/apis/transfer.ts#updateStockTransfers', async () => {
    const r = await app.call('src/apis/transfer.ts#updateStockTransfers', {}, await legacyBody());
    expect(r.status).toBe(500);
  }, { characterise: '500 null source' });

  knownGap('POST /stockTransfers with the legacy COMPLETED body moves the stock',
    'StockTransferApiController.create -> completeStockTransfer publishes StockTransferEvent(null id) ("null source"): rollback, 500 (legacy Transfer screen)',
    'src/apis/transfer.ts#updateStockTransfers', async () => {
      (await app.call('src/apis/transfer.ts#updateStockTransfers', {}, await legacyBody())).holds();
    });
});
