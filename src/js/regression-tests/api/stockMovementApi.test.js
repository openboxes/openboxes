import stockMovementApi from 'api/services/StockMovementApi';
import stockMovementItemApi from 'api/services/StockMovementItemApi';
import stockTransferApi from 'api/services/StockTransferApi';
import apiClient from 'utils/apiClient';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

describe('StockMovementApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('createStockMovement posts the new movement and returns the response', async () => {
    const response = { data: { data: { id: 'sm1' } } };
    apiClient.post.mockResolvedValue(response);

    await expect(stockMovementApi.createStockMovement({ origin: { id: 'l1' } })).resolves.toBe(response);
    expect(apiClient.post).toHaveBeenCalledWith('/api/stockMovements', { origin: { id: 'l1' } });
  });

  it('passes a failed request on to the caller', async () => {
    const error = new Error('Request failed with status code 500');
    apiClient.get.mockRejectedValue(error);

    await expect(stockMovementApi.getStockMovements({ params: {} })).rejects.toBe(error);
  });

  it.each([
    ['updateStockMovement', 'post', ['sm1', { name: 'n' }], ['/api/stockMovements/sm1/updateRequisition', { name: 'n' }]],
    ['getStockMovements', 'get', [{ params: { direction: 'INBOUND' } }], ['/api/stockMovements', { params: { direction: 'INBOUND' } }]],
    ['deleteStockMovement', 'delete', ['sm1'], ['/api/stockMovements/sm1']],
    ['updateStatus', 'post', ['sm1', { status: 'PICKING' }], ['/api/stockMovements/sm1/status', { status: 'PICKING' }]],
    ['rollbackApproval', 'put', ['sm1'], ['/api/stockMovements/sm1/rollbackApproval']],
    ['getStockMovementById', 'get', ['sm1', { stepNumber: 2 }], ['/api/stockMovements/sm1', { params: { stepNumber: 2 } }]],
    ['getStockMovementItems', 'get', ['sm1', { offset: 0, max: 10 }], ['/api/stockMovements/sm1/stockMovementItems', { params: { offset: 0, max: 10 } }]],
    ['updateShipment', 'post', ['sm1', { driverName: 'd' }], ['/api/stockMovements/sm1/updateShipment', { driverName: 'd' }]],
    ['getDocuments', 'get', ['sm1'], ['/api/stockMovements/sm1/documents']],
  ])('%s sends a %s', (method, httpMethod, args, expectedCall) => {
    stockMovementApi[method](...args);

    expect(apiClient[httpMethod]).toHaveBeenCalledWith(...expectedCall);
  });

  it('rejectRequest sets the REJECTED status with who sent, who receives and why', () => {
    stockMovementApi.rejectRequest({
      id: 'sm1', sender: { id: 'u1' }, recipient: { id: 'u2' }, comment: 'Out of stock', extra: 'ignored',
    });

    expect(apiClient.post).toHaveBeenCalledWith('/api/stockMovements/sm1/status', {
      status: 'REJECTED',
      sender: { id: 'u1' },
      recipient: { id: 'u2' },
      comment: 'Out of stock',
    });
  });

  it('importCsv uploads the form to the stock movement page controller', () => {
    const formData = new FormData();
    stockMovementApi.importCsv('sm1', formData);

    expect(apiClient.post).toHaveBeenCalledWith('/openboxes/stockMovement/importCsv/sm1', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  });

  it('exportCsv downloads the items as a blob', () => {
    stockMovementApi.exportCsv('sm1');

    expect(apiClient.get).toHaveBeenCalledWith('/openboxes/stockMovement/exportCsv/sm1', { responseType: 'blob' });
  });

  it('uploadDocuments uploads the form to the stock movement page controller', () => {
    const formData = new FormData();
    stockMovementApi.uploadDocuments('sm1', formData);

    expect(apiClient.post).toHaveBeenCalledWith('/openboxes/stockMovement/uploadDocuments/sm1', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  });
});

describe('StockMovementItemApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('revertPick deletes the picklist items of the line', () => {
    stockMovementItemApi.revertPick('i1');

    expect(apiClient.delete).toHaveBeenCalledWith('/api/stockMovementItems/i1/picklistItems');
  });
});

describe('StockTransferApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it.each([
    ['getStockTransfers', 'get', [{ params: { max: 5 } }], ['/api/stockTransfers', { params: { max: 5 } }]],
    ['getStockTransfer', 'get', ['st1'], ['/api/stockTransfers/st1']],
    ['updateStockTransfer', 'post', ['st1', { status: 'PENDING' }], ['/api/stockTransfers/st1', { status: 'PENDING' }]],
    ['deleteStockTransfer', 'delete', ['st1'], ['/api/stockTransfers/st1']],
    ['removeItem', 'delete', ['sti1'], ['/api/stockTransferItems/sti1']],
    ['removeAllItems', 'delete', ['st1'], ['/api/stockTransfers/st1/removeAllItems']],
  ])('%s sends a %s', (method, httpMethod, args, expectedCall) => {
    stockTransferApi[method](...args);

    expect(apiClient[httpMethod]).toHaveBeenCalledWith(...expectedCall);
  });

  it('getStockTransferCandidates asks for the location candidates, expired only when asked', () => {
    stockTransferApi.getStockTransferCandidates('l 1', true);
    stockTransferApi.getStockTransferCandidates('l1', false);

    expect(apiClient.get).toHaveBeenNthCalledWith(1, '/api/stockTransfers/candidates?location.id=l%201&showExpiredItemsOnly=true');
    expect(apiClient.get).toHaveBeenNthCalledWith(2, '/api/stockTransfers/candidates?location.id=l1&showExpiredItemsOnly=false');
  });

  it('getStockTransferCandidates leaves out an unset flag', () => {
    stockTransferApi.getStockTransferCandidates('l1');

    expect(apiClient.get).toHaveBeenCalledWith('/api/stockTransfers/candidates?location.id=l1');
  });
});
