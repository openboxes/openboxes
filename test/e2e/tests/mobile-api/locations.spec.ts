/** Locations: picker, lookups by scanned code, bin searches, product summary, bin details. Main Warehouse. */
import { contractTest, expect, knownGap, test } from '../../lib/ts/test';
import { MobileApp } from '../../lib/ts/app';
import { MAIN_WAREHOUSE, uid } from '../../lib/ts/env';
import { days } from '../../lib/ts/fixtures';

test.describe('locations', () => {
  let app: MobileApp;
  let bin: { id: string; number: string };
  let p: { id: string; productCode: string };

  test.beforeEach(async ({ fx }) => {
    app = await MobileApp.signIn(MAIN_WAREHOUSE);
    bin = await fx.bin(MAIN_WAREHOUSE, 'LC-BIN', ['PICK_STOCK', 'PUTAWAY_STOCK']);
    p = await fx.product('LC');
    await fx.stock(MAIN_WAREHOUSE, p.id, [[bin.id, 7, `LOT-${uid()}`, days(365)]]);
  });
  test.afterEach(async () => { await app.dispose(); });

  contractTest('GET /locations?locationTypeCode=DEPOT&activityCodes=MANAGE_INVENTORY&applyUserFilter=true lists the depots', 'src/apis/locations.ts#getLocations', async () => {
    const r = await app.call('src/apis/locations.ts#getLocations');
    r.holds();
    expect(r.data.data.map((l: any) => l.id)).toContain(MAIN_WAREHOUSE);
  });

  contractTest('GET /locations/{scanned location number} answers the bin (Scan screen)', 'src/apis/locations.ts#searchLocationByLocationNumber', async () => {
    const r = await app.call('src/apis/locations.ts#searchLocationByLocationNumber', { locationNumber: bin.number });
    r.holds();
    expect(r.data.data.id).toBe(bin.id);
  });

  contractTest('GET /locations/{code} answers the bin (Create Transfer)', 'src/apis/createTransfer.ts#lookupLocationByCode', async () => {
    const r = await app.call('src/apis/createTransfer.ts#lookupLocationByCode', { code: bin.number });
    r.holds();
    expect(r.data.data.id).toBe(bin.id);
  });

  contractTest('GET /locations/{id}/productSummary answers the stock summary of the location', 'src/apis/locations.ts#fetchProductSummary', async () => {
    const r = await app.call('src/apis/locations.ts#fetchProductSummary', { id: MAIN_WAREHOUSE });
    r.holds();
    expect(r.data.data.find((x: any) => x.productCode === p.productCode)?.quantityOnHand).toBe(7);
  });

  contractTest('GET /internalLocations answers (saga wired, never dispatched)', 'src/apis/locations.ts#getBinLocations', async () => {
    (await app.call('src/apis/locations.ts#getBinLocations')).holds();
  });

  contractTest('GET /internalLocations/?location.id= answers the bins of the facility (saga wired, never dispatched)', 'src/apis/locations.ts#internalLocations', async () => {
    const r = await app.call('src/apis/locations.ts#internalLocations', { id: MAIN_WAREHOUSE });
    expect(r.status).toBe(200);
  }, { characterise: 'answers 200 with {data:[...]}; the saga reads a bare array' });

  knownGap('GET /internalLocations/?location.id= answers a bare array as the saga expects',
    '(dead path) InternalLocationApiController.list answers {data:[...]}, src/apis/locations.ts internalLocations reads a bare array',
    'src/apis/locations.ts#internalLocations', async () => {
      const r = await app.call('src/apis/locations.ts#internalLocations', { id: MAIN_WAREHOUSE });
      expect(Array.isArray(r.json)).toBe(true);
    });

  contractTest('GET /internalLocations/search with a typed term (global search) finds the bin', 'src/apis/locations.ts#searchInternalLocations', async () => {
    const r = await app.call('src/apis/locations.ts#searchInternalLocations', { searchTerm: bin.number });
    r.holds();
    expect(r.data.data.map((l: any) => l.id)).toContain(bin.id);
  });

  knownGap('GET /internalLocations/search with the screens empty term lists the facility bins',
    '(minLength guard) searchInternalLocations with searchTerm= (empty) answers [] under openboxes.typeahead.minLength, so every bin picker is empty',
    'src/apis/locations.ts#searchInternalLocations', async () => {
      const r = await app.call('src/apis/locations.ts#searchInternalLocations',
        { searchTerm: '', additionalParams: { 'parentLocation.id': MAIN_WAREHOUSE, locationTypeCode: 'BIN_LOCATION', max: 25, offset: 0 } });
      r.holds();
      expect(r.data.data.map((l: any) => l.id)).toContain(bin.id);
    });

  contractTest('GET /internalLocations/search for destination bins finds the bin by its code', 'src/apis/createTransfer.ts#searchDestinationBins', async () => {
    const r = await app.call('src/apis/createTransfer.ts#searchDestinationBins', { searchTerm: bin.number });
    r.holds();
    expect(r.data.data.map((l: any) => l.id)).toContain(bin.id);
  });

  knownGap('GET /internalLocations/search for destination bins leaves out receiving bins',
    'ignoreActivityCodes=RECEIVE_STOCK is ignored by InternalLocationApiController.search, receiving bins are offered as transfer destinations',
    'src/apis/createTransfer.ts#searchDestinationBins', async ({ fx }) => {
      const receiving = await fx.bin(MAIN_WAREHOUSE, 'RCVX', ['RECEIVE_STOCK']);
      const r = await app.call('src/apis/createTransfer.ts#searchDestinationBins', { searchTerm: receiving.number });
      r.holds([], []);
      expect(r.data.data.map((l: any) => l.id)).not.toContain(receiving.id);
    });

  knownGap('GET /internalLocations/{id}/details answers the bin detail',
    '(no route) OBAF maps no /internalLocations/{id}/details and InternalLocationApiController has no details action',
    'src/apis/locations.ts#internalLocationDetails', async () => {
      (await app.call('src/apis/locations.ts#internalLocationDetails', { id: bin.id })).holds();
    });

  knownGap('GET /internalLocations/{id}/details?location.id= answers the bin with its stock',
    '(no route) OBAF maps no /internalLocations/{id}/details',
    'src/apis/locations.ts#internalLocationsDetails', async () => {
      (await app.call('src/apis/locations.ts#internalLocationsDetails', { id: bin.id, location: MAIN_WAREHOUSE })).holds();
    });

  /** The unmapped URL itself: what the app gets today (characterised, so a change is noticed). */
  contractTest('GET /internalLocations/{id}/details today: not a 2xx (the route does not exist)', ['src/apis/locations.ts#internalLocationDetails', 'src/apis/locations.ts#internalLocationsDetails'], async () => {
    const a = await app.call('src/apis/locations.ts#internalLocationDetails', { id: bin.id });
    const b = await app.call('src/apis/locations.ts#internalLocationsDetails', { id: bin.id, location: MAIN_WAREHOUSE });
    expect(a.status).toBeGreaterThanOrEqual(400);
    expect(b.status).toBeGreaterThanOrEqual(400);
  }, { characterise: 'no route: the app gets an error' });

  knownGap('an unmapped API URL answers 404',
    'with the JSON "404" mapping commented out, an unmapped /api URL fails in SecurityInterceptor ("No Session found for current thread"): 500, not 404',
    'src/apis/locations.ts#internalLocationDetails', async () => {
      expect((await app.call('src/apis/locations.ts#internalLocationDetails', { id: bin.id })).status).toBe(404);
    });
});
