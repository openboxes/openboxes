// Not covered on OBAF (OBAF lacks the code): the eight ReceivingApi.* rows
// (getReceiptSummary, startReceipt, syncReceiptLines, updateItemsBatch, completeReceipt,
// editReceivingInfo, createReceiptItemComment, updateReceiptItemComment).
import fulfillmentApi from 'api/services/FulfillmentApi';
import genericApi from 'api/services/GenericApi';
import glAccountApi from 'api/services/GlAccountApi';
import indicatorsApi from 'api/services/IndicatorsApi';
import organizationApi from 'api/services/OrganizationApi';
import packingListApi from 'api/services/PackingListApi';
import productClassificationApi from 'api/services/ProductClassificationApi';
import productGroupApi from 'api/services/ProductGroupApi';
import productPackageApi from 'api/services/ProductPackageApi';
import productSupplierApi from 'api/services/ProductSupplierApi';
import productSupplierAttributeApi from 'api/services/ProductSupplierAttributeApi';
import productSupplierPreferenceApi from 'api/services/ProductSupplierPreferenceApi';
import purchaseOrderApi from 'api/services/PurchaseOrderApi';
import selectOptionsApi from 'api/services/SelectOptionsApi';
import stockListApi from 'api/services/StockListApi';
import unitOfMeasureApi from 'api/services/UnitOfMeasureApi';
import userApi from 'api/services/UserApi';
import apiClient, { apiClientCustomResponseHandler } from 'utils/apiClient';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

const config = { params: { max: 10 } };
const payload = { id: 'x1', name: 'Name' };

describe('API services sending one request each', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it.each([
    ['GenericApi.getShipmentTypes', () => genericApi.getShipmentTypes(), 'get', ['/api/generic/shipmentType']],
    ['GlAccountApi.getGlAccountOptions', () => glAccountApi.getGlAccountOptions(config), 'get', ['/api/glAccountOptions', config]],
    ['OrganizationApi.getOrganization', () => organizationApi.getOrganization('o1'), 'get', ['/api/generic/organization/o1']],
    ['ProductClassificationApi.getProductClassifications', () => productClassificationApi.getProductClassifications('f1'), 'get', ['/api/facilities/f1/products/classifications']],
    ['ProductGroupApi.getProductGroupsOptions', () => productGroupApi.getProductGroupsOptions(), 'get', ['/api/productGroupOptions']],
    ['ProductPackageApi.save', () => productPackageApi.save(payload), 'post', ['/api/productPackages', payload]],
    ['ProductSupplierApi.getPreferenceTypeOptions', () => productSupplierApi.getPreferenceTypeOptions(config), 'get', ['/api/preferenceTypeOptions', config]],
    ['ProductSupplierApi.getRatingTypeOptions', () => productSupplierApi.getRatingTypeOptions(config), 'get', ['/api/ratingTypeCodeOptions', config]],
    ['ProductSupplierApi.deleteProductSupplier', () => productSupplierApi.deleteProductSupplier('ps1'), 'delete', ['/api/productSuppliers/ps1']],
    ['ProductSupplierApi.getProductSupplier', () => productSupplierApi.getProductSupplier('ps1'), 'get', ['/api/productSuppliers/ps1']],
    ['ProductSupplierApi.getAttributes', () => productSupplierApi.getAttributes(config), 'get', ['/api/attributes', config]],
    ['ProductSupplierApi.deleteProductSupplierPreference', () => productSupplierApi.deleteProductSupplierPreference('psp1'), 'delete', ['/api/productSupplierPreferences/psp1']],
    ['ProductSupplierApi.saveDetails', () => productSupplierApi.saveDetails(payload), 'post', ['/api/productSuppliers', payload]],
    ['ProductSupplierApi.updateDetails', () => productSupplierApi.updateDetails(payload, 'ps1'), 'put', ['/api/productSuppliers/ps1', payload]],
    ['ProductSupplierAttributeApi.updateAttributes', () => productSupplierAttributeApi.updateAttributes([payload]), 'post', ['/api/productSupplierAttributes/batch', [payload]]],
    ['ProductSupplierPreferenceApi.saveOrUpdateBatch', () => productSupplierPreferenceApi.saveOrUpdateBatch([payload]), 'post', ['/api/productSupplierPreferences/batch', [payload]]],
    ['PurchaseOrderApi.deleteOrder', () => purchaseOrderApi.deleteOrder('po1'), 'delete', ['/api/purchaseOrders/po1']],
    ['PurchaseOrderApi.rollbackOrder', () => purchaseOrderApi.rollbackOrder('po1'), 'post', ['/api/purchaseOrders/po1/rollback']],
    ['PurchaseOrderApi.getOrders', () => purchaseOrderApi.getOrders(config), 'get', ['/api/purchaseOrders', config]],
    ['PurchaseOrderApi.getPaymentTerms', () => purchaseOrderApi.getPaymentTerms(config), 'get', ['/api/paymentTermOptions', config]],
    ['SelectOptionsApi.getHandlingRequirementsOptions', () => selectOptionsApi.getHandlingRequirementsOptions(), 'get', ['/api/handlingRequirementsOptions']],
    ['StockListApi.getStockLists', () => stockListApi.getStockLists(config), 'get', ['/api/stocklists', config]],
    ['StockListApi.deleteStockList', () => stockListApi.deleteStockList('sl1'), 'delete', ['/api/stocklists/sl1']],
    ['StockListApi.clearStockList', () => stockListApi.clearStockList('sl1'), 'post', ['/api/stocklists/sl1/clear']],
    ['StockListApi.cloneStockList', () => stockListApi.cloneStockList('sl1'), 'post', ['/api/stocklists/sl1/clone']],
    ['StockListApi.publishStockList', () => stockListApi.publishStockList('sl1'), 'post', ['/api/stocklists/sl1/publish']],
    ['StockListApi.unpublishStockList', () => stockListApi.unpublishStockList('sl1'), 'post', ['/api/stocklists/sl1/unpublish']],
    ['UnitOfMeasureApi.getCurrenciesOptions', () => unitOfMeasureApi.getCurrenciesOptions(), 'get', ['/api/unitOfMeasure/currencies']],
    ['UnitOfMeasureApi.getUnitOfMeasureOptions', () => unitOfMeasureApi.getUnitOfMeasureOptions(config), 'get', ['/api/unitOfMeasures/options', config]],
    ['UserApi.getUsersOptions', () => userApi.getUsersOptions(config), 'get', ['/api/users', config]],
  ])('%s sends its request and returns the response', async (name, call, httpMethod, expectedCall) => {
    const response = { data: { data: name } };
    apiClient[httpMethod].mockResolvedValue(response);

    await expect(call()).resolves.toBe(response);
    expect(apiClient[httpMethod]).toHaveBeenCalledTimes(1);
    expect(apiClient[httpMethod]).toHaveBeenCalledWith(...expectedCall);
  });

  it('passes a failed request on to the caller', async () => {
    const error = new Error('Request failed with status code 403');
    apiClient.delete.mockRejectedValue(error);

    await expect(stockListApi.deleteStockList('sl1')).rejects.toBe(error);
  });
});

describe('IndicatorsApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it.each([
    ['getProductsInventoried', '/api/reports/indicators/productsInventoried'],
    ['getInventoryAccuracy', '/api/reports/indicators/inventoryAccuracy'],
    ['getInventoryShrinkage', '/api/reports/indicators/inventoryShrinkage'],
  ])('%s sends the filters as parameters, none by default', (method, url) => {
    indicatorsApi[method]({ facilityId: 'f1', startDate: '2026-01-01' });
    indicatorsApi[method]();

    expect(apiClient.get).toHaveBeenNthCalledWith(1, url, { params: { facilityId: 'f1', startDate: '2026-01-01' } });
    expect(apiClient.get).toHaveBeenNthCalledWith(2, url, { params: {} });
  });
});

describe('FulfillmentApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it.each([
    ['createOutbound', '/api/fulfillments'],
    ['validateOutbound', '/api/fulfillments/validate'],
  ])('%s posts through the client that leaves error handling to the caller', async (method, url) => {
    const error = { response: { status: 400, data: { errorMessages: ['bad row'] } } };
    apiClientCustomResponseHandler.post.mockRejectedValue(error);

    await expect(fulfillmentApi[method](payload)).rejects.toBe(error);
    expect(apiClientCustomResponseHandler.post).toHaveBeenCalledWith(url, payload);
    expect(apiClient.post).not.toHaveBeenCalled();
  });
});

describe('PackingListApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('importPackingList uploads the file as multipart form data to the packing list page controller', () => {
    const file = new File(['a'], 'packing.xls');

    packingListApi.importPackingList(file);

    const [url, body, requestConfig] = apiClient.post.mock.calls[0];
    expect(url).toMatch(/\/packingList\/upload$/);
    expect(body.get('importFile')).toBe(file);
    expect(requestConfig).toEqual({ headers: { 'content-type': 'multipart/form-data' } });
  });
});
