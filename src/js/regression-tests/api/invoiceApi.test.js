import invoiceApi from 'api/services/InvoiceApi';
import invoiceItemApi from 'api/services/InvoiceItemApi';
import picklistApi from 'api/services/PicklistApi';
import prepaymentInvoiceApi from 'api/services/PrepaymentInvoiceApi';
import prepaymentInvoiceItemApi from 'api/services/PrepaymentInvoiceItemApi';
import apiClient, { apiClientCustomResponseHandler } from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('utils/file-download-util', () => ({ __esModule: true, default: jest.fn() }));

// jsdom's Blob has no text(): read it the way a browser without it would.
const readBlob = (blob) => new Promise((resolve) => {
  const reader = new FileReader();
  reader.onload = () => resolve(reader.result);
  reader.readAsText(blob);
});

describe('InvoiceApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it.each([
    ['getInvoices', 'get', [{ params: { max: 5 } }], ['/api/invoices', { params: { max: 5 } }]],
    ['getInvoice', 'get', ['inv1', { params: { a: 1 } }], ['/api/invoices/inv1', { params: { a: 1 } }]],
    ['saveInvoice', 'post', ['inv1', { vendor: 'v' }], ['/api/invoices/inv1', { vendor: 'v' }]],
    ['submitInvoice', 'post', ['inv1'], ['/api/invoices/inv1/submit']],
    ['postInvoice', 'post', ['inv1'], ['/api/invoices/inv1/post']],
    ['getInvoiceItems', 'get', ['inv1', { params: { max: 10 } }], ['/api/invoices/inv1/items', { params: { max: 10 } }]],
    ['saveInvoiceItems', 'post', ['inv1', [{ id: 'ii1' }]], ['/api/invoices/inv1/items', [{ id: 'ii1' }]]],
    ['removeInvoiceItem', 'delete', ['ii1'], ['/api/invoices/ii1/removeItem']],
    ['getInvoiceOrders', 'get', ['inv1', { params: {} }], ['/api/invoices/inv1/orders', { params: {} }]],
    ['getInvoiceShipments', 'get', ['inv1', { params: {} }], ['/api/invoices/inv1/shipments', { params: {} }]],
    // The second argument is the request body (the selected candidates).
    ['saveInvoiceItemsCandidates', 'post', ['inv1', [{ id: 'c1' }]], ['/api/invoices/inv1/invoiceItemCandidates', [{ id: 'c1' }]]],
  ])('%s sends a %s', (method, httpMethod, args, expectedCall) => {
    invoiceApi[method](...args);

    expect(apiClient[httpMethod]).toHaveBeenCalledWith(...expectedCall);
  });

  it('returns what the client resolves with', async () => {
    const response = { data: { data: { id: 'inv1' } } };
    apiClient.get.mockResolvedValue(response);

    await expect(invoiceApi.getInvoice('inv1')).resolves.toBe(response);
  });

  it('downloadInvoices exports the invoice list with the current filters', async () => {
    exportFileFromAPI.mockResolvedValue('done');

    await expect(invoiceApi.downloadInvoices({ status: 'POSTED' })).resolves.toBe('done');
    expect(exportFileFromAPI).toHaveBeenCalledWith({ url: '/api/invoices', params: { status: 'POSTED' } });
  });
});

describe('InvoiceItemApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('validateInvoiceItem posts the item to its validation endpoint, leaving errors to the caller', async () => {
    const error = { response: { status: 400 } };
    apiClientCustomResponseHandler.post.mockRejectedValue(error);
    const item = { id: 'ii1', quantity: 2 };

    await expect(invoiceItemApi.validateInvoiceItem(item)).rejects.toBe(error);
    expect(apiClientCustomResponseHandler.post).toHaveBeenCalledWith('/api/invoiceItems/ii1/validation', item);
  });
});

describe('Prepayment invoice APIs', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('updateInvoiceItems posts the items of the prepayment invoice', () => {
    prepaymentInvoiceApi.updateInvoiceItems('inv1', [{ id: 'ii1' }]);

    expect(apiClient.post).toHaveBeenCalledWith('/api/prepaymentInvoices/inv1/invoiceItems', [{ id: 'ii1' }]);
  });

  it('deletePrepaymentInvoiceItem deletes the item', () => {
    prepaymentInvoiceItemApi.deletePrepaymentInvoiceItem('ii1');

    expect(apiClient.delete).toHaveBeenCalledWith('/api/prepaymentInvoiceItems/ii1');
  });
});

describe('PicklistApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('clearPicklist deletes the picklist items', () => {
    picklistApi.clearPicklist('pl1');

    expect(apiClient.delete).toHaveBeenCalledWith('/api/picklists/pl1/items');
  });

  it.each([
    ['exportPicklistItems', '/api/stockMovements/exportPickListItems/sm1', 'PickListItems'],
    ['exportPicklistTemplate', '/api/stockMovements/picklistTemplate/sm1', 'PickListItems-template'],
    ['exportPacklistTemplate', '/api/stockMovements/packlistTemplate/sm1', 'PackListItems-template'],
  ])('%s downloads a csv with a default name', (method, url, filename) => {
    picklistApi[method]('sm1', {});

    expect(exportFileFromAPI).toHaveBeenCalledWith({ url, filename, format: 'csv' });
  });

  it.each([
    ['exportPicklistItems', '/api/stockMovements/exportPickListItems/sm1'],
    ['exportPicklistTemplate', '/api/stockMovements/picklistTemplate/sm1'],
    ['exportPacklistTemplate', '/api/stockMovements/packlistTemplate/sm1'],
  ])('%s uses the format and file name it is given', (method, url) => {
    picklistApi[method]('sm1', { format: 'xls', fileName: 'Mine' });

    expect(exportFileFromAPI).toHaveBeenCalledWith({ url, filename: 'Mine', format: 'xls' });
  });

  it.each([
    ['importPicklist', '/api/stockMovements/importPickListItems/sm1'],
    ['importPacklist', '/api/stockMovements/importPackListItems/sm1'],
  ])('%s uploads the file content as csv, whatever type the browser gave it', async (method, url) => {
    const file = new File(['code,qty\nA,1'], 'items.txt', { type: 'text/plain' });

    picklistApi[method]('sm1', file);

    const [calledUrl, body, config] = apiClient.post.mock.calls[0];
    expect(calledUrl).toBe(url);
    expect(config).toEqual({ headers: { 'content-type': 'multipart/form-data' } });
    const uploaded = body.get('importFile');
    expect(uploaded.type).toBe('text/csv');
    expect(uploaded.size).toBe(file.size);
    expect(await readBlob(uploaded)).toBe('code,qty\nA,1');
  });
});
