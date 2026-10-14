// Not covered on OBAF (OBAF lacks the code): ProductApi.getAvailabilityInAllDepots,
// the whole InventoryLevelApi describe (getPreferredBinLocations) and
// LocationApi.getReceivingInternalLocations.
import locationApi from 'api/services/LocationApi';
import productApi from 'api/services/ProductApi';
import apiClient, { apiClientCustomResponseHandler } from 'utils/apiClient';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

// The query string the request config would produce.
const serializedQuery = (config) => config.paramsSerializer(config.params);

describe('ProductApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('getProducts passes the config through', () => {
    productApi.getProducts({ params: { q: 'abc' } });

    expect(apiClient.get).toHaveBeenCalledWith('/api/products', { params: { q: 'abc' } });
  });

  // KNOWN-GAP: OBAF's getInventoryItem calls axios.get directly,
  // not apiClientCustomResponseHandler.
  it.skip('getInventoryItem uses the client without error notifications and lets the caller see a missing lot', async () => {
    const error = { response: { status: 404 } };
    apiClientCustomResponseHandler.get.mockRejectedValue(error);

    await expect(productApi.getInventoryItem('p1', 'L1')).rejects.toBe(error);
    const [url] = apiClientCustomResponseHandler.get.mock.calls[0];
    expect(url).toMatch(/\/api\/products\/p1\/inventoryItems\/L1$/);
    expect(apiClient.get).not.toHaveBeenCalled();
  });

  it('getProduct reads the product through the generic API', async () => {
    const response = { data: { data: { id: 'p1' } } };
    apiClient.get.mockResolvedValue(response);

    await expect(productApi.getProduct('p1')).resolves.toBe(response);
    expect(apiClient.get).toHaveBeenCalledWith('/api/generic/product/p1');
  });

  it('getLatestInventoryCountDate sends the product ids as repeated parameters', () => {
    productApi.getLatestInventoryCountDate(['p1', 'p2']);

    const [url, config] = apiClient.get.mock.calls[0];
    expect(url).toBe('/api/products/getLatestInventoryCountDate');
    expect(config.params).toEqual({ productIds: ['p1', 'p2'] });
    expect(serializedQuery(config)).toBe('productIds=p1&productIds=p2');
  });

  it('getLotNumbersByProductIds sends the product ids as repeated parameters', () => {
    productApi.getLotNumbersByProductIds(['p1', 'p2']);

    const [url, config] = apiClient.get.mock.calls[0];
    expect(url).toBe('/api/products/inventoryItems/lotNumbersWithExpirationDate');
    expect(serializedQuery(config)).toBe('productIds=p1&productIds=p2');
  });

  it('availableItems filters by location and products', () => {
    productApi.availableItems({ locationId: 'l1', productIds: ['p1', 'p 2'] });

    const [url, config] = apiClient.get.mock.calls[0];
    expect(url).toBe('/api/products/availableItems');
    expect(config.params).toEqual({ 'product.id': ['p1', 'p 2'], 'location.id': 'l1' });
    expect(serializedQuery(config)).toBe('location.id=l1&product.id=p1&product.id=p%202');
  });
});

describe('LocationApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('getLocations calls the client directly with the config', async () => {
    const response = { data: { data: [] } };
    apiClient.mockResolvedValue(response);

    await expect(locationApi.getLocations({ params: { max: 10 } })).resolves.toBe(response);
    expect(apiClient).toHaveBeenCalledWith('/api/locations', { params: { max: 10 } });
  });

  it('getLocationTypes passes the config through', () => {
    locationApi.getLocationTypes({ params: { a: 1 } });

    expect(apiClient.get).toHaveBeenCalledWith('/api/locations/locationTypes', { params: { a: 1 } });
  });

  it('createLocation posts the location with the extra parameters', () => {
    locationApi.createLocation({ name: 'Depot' }, { useDefaultActivities: true });

    expect(apiClient.post).toHaveBeenCalledWith(
      '/api/locations',
      { name: 'Depot' },
      { params: { useDefaultActivities: true } },
    );
  });

  it('updateLocationAddress posts only the address', () => {
    locationApi.updateLocationAddress('l1', { city: 'Boston' });

    expect(apiClient.post).toHaveBeenCalledWith('/api/locations/l1', { address: { city: 'Boston' } });
  });
});
