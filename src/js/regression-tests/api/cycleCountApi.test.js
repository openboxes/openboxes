import cycleCountApi from 'api/services/CycleCountApi';
import apiClient, { apiClientCustomResponseHandler } from 'utils/apiClient';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

const BASE = '/api/facilities/f1/cycle-counts';

describe('CycleCountApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('createRequest posts the payload to the facility request batch and returns the response', async () => {
    const response = { data: { data: [{ id: 'r1' }] } };
    apiClient.post.mockResolvedValue(response);

    await expect(cycleCountApi.createRequest({ requests: [] }, 'f1')).resolves.toBe(response);
    expect(apiClient.post).toHaveBeenCalledWith(`${BASE}/requests/batch`, { requests: [] });
  });

  it('deleteRequests sends every id as a repeated query parameter', () => {
    cycleCountApi.deleteRequests('f1', ['a', 'b c']);

    expect(apiClient.delete).toHaveBeenCalledWith(`${BASE}/requests/batch?id=a&id=b%20c`);
  });

  it('deleteRequests with a single id', () => {
    cycleCountApi.deleteRequests('f1', 'a');

    expect(apiClient.delete).toHaveBeenCalledWith(`${BASE}/requests/batch?id=a`);
  });

  describe.each([
    ['startCount', `${BASE}/start/batch`],
    ['startRecount', `${BASE}/recount/start/batch`],
  ])('%s', (method, url) => {
    it('posts the payload with an empty config by default', () => {
      cycleCountApi[method]({ payload: { ids: ['cc1'] }, locationId: 'f1' });

      expect(apiClient.post).toHaveBeenCalledWith(url, { ids: ['cc1'] }, {});
    });

    it('asks for a printable format and passes the request config through', () => {
      const config = { responseType: 'blob' };
      cycleCountApi[method]({
        payload: { ids: ['cc1'] }, locationId: 'f1', format: 'pdf', config,
      });

      expect(apiClient.post).toHaveBeenCalledWith(`${url}?format=pdf`, { ids: ['cc1'] }, config);
    });
  });

  it('getCycleCounts filters by ids and sorts', () => {
    cycleCountApi.getCycleCounts('f1', ['cc1', 'cc2'], 'binLocation');

    expect(apiClient.get).toHaveBeenCalledWith(`${BASE}?id=cc1&id=cc2&sortBy=binLocation`);
  });

  it('getCycleCounts leaves out a missing sort', () => {
    cycleCountApi.getCycleCounts('f1', ['cc1']);

    expect(apiClient.get).toHaveBeenCalledWith(`${BASE}?id=cc1`);
  });

  it('deleteCycleCountItem deletes the item', () => {
    cycleCountApi.deleteCycleCountItem('f1', 'ci1');

    expect(apiClient.delete).toHaveBeenCalledWith(`${BASE}/items/ci1`);
  });

  it('submitCount posts through the notifying client', () => {
    cycleCountApi.submitCount({ items: [] }, 'f1', 'cc1');

    expect(apiClient.post).toHaveBeenCalledWith(`${BASE}/cc1/count`, { items: [] });
    expect(apiClientCustomResponseHandler.post).not.toHaveBeenCalled();
  });

  it('submitRecount posts through the client that leaves error handling to the caller', async () => {
    const error = new Error('Validation failed');
    apiClientCustomResponseHandler.post.mockRejectedValue(error);

    await expect(cycleCountApi.submitRecount({ items: [] }, 'f1', 'cc1')).rejects.toBe(error);
    expect(apiClientCustomResponseHandler.post).toHaveBeenCalledWith(`${BASE}/cc1/recount`, { items: [] });
    expect(apiClient.post).not.toHaveBeenCalled();
  });

  it('refreshItems posts an empty body with the count index as a parameter', () => {
    cycleCountApi.refreshItems('f1', 'cc1', true, 1);

    expect(apiClient.post).toHaveBeenCalledWith(
      `${BASE}/cc1/refresh?removeOutOfStockItemsImplicitly=true`,
      {},
      { params: { countIndex: 1 } },
    );
  });

  it('refreshItems keeps out-of-stock items unless asked to remove them', () => {
    cycleCountApi.refreshItems('f1', 'cc1', false, 0);

    expect(apiClient.post).toHaveBeenCalledWith(`${BASE}/cc1/refresh`, {}, { params: { countIndex: 0 } });
  });

  it.each([
    ['createCycleCountItems', 'post', ['cc1'], `${BASE}/cc1/items/batch`],
    ['createCycleCountItemsBatch', 'post', [], `${BASE}/items/batch`],
    ['updateCycleCountItems', 'patch', ['cc1'], `${BASE}/cc1/items/batch`],
    ['updateCycleCountItemsBatch', 'patch', [], `${BASE}/items/batch`],
  ])('%s sends a %s to %s', (method, httpMethod, extraArgs, url) => {
    const payload = [{ id: 'ci1', quantityCounted: 3 }];
    cycleCountApi[method](payload, 'f1', ...extraArgs);

    expect(apiClient[httpMethod]).toHaveBeenCalledWith(url, payload);
  });

  it('updateCycleCountRequests patches the request batch (location first)', () => {
    cycleCountApi.updateCycleCountRequests('f1', [{ id: 'r1' }]);

    expect(apiClient.patch).toHaveBeenCalledWith(`${BASE}/requests/batch`, [{ id: 'r1' }]);
  });

  it.each([
    ['importCycleCountItems', `${BASE}/items/upload/count`],
    ['importCycleCountRecountItems', `${BASE}/items/upload/recount`],
  ])('%s uploads the file as multipart form data', (method, url) => {
    const file = new File(['a,b'], 'count.csv', { type: 'text/csv' });

    cycleCountApi[method](file, 'f1');

    const [calledUrl, body, config] = apiClient.post.mock.calls[0];
    expect(calledUrl).toBe(url);
    expect(body).toBeInstanceOf(FormData);
    expect(body.get('importFile')).toBe(file);
    expect(config).toEqual({ headers: { 'content-type': 'multipart/form-data' } });
  });

  it('getPendingRequests filters by request ids and limits the result', () => {
    cycleCountApi.getPendingRequests({ locationId: 'f1', requestIds: ['r1', 'r2'], max: 10 });

    expect(apiClient.get).toHaveBeenCalledWith(`${BASE}/requests/pending?max=10&requestIds=r1&requestIds=r2`);
  });

  it('getPendingRequests without filters asks for everything', () => {
    cycleCountApi.getPendingRequests({ locationId: 'f1' });

    expect(apiClient.get).toHaveBeenCalledWith(`${BASE}/requests/pending?`);
  });
});
