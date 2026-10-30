import apiClient, {
  apiClientCustomResponseHandler,
  handleError,
  handleSuccess,
  handleValidationErrors,
  justRejectRequestError,
  mapToEmptyString,
  parseResponse,
  urlInterceptor,
} from 'utils/apiClient';

jest.mock('components/Layout/notifications/notification', () => ({
  __esModule: true,
  default: jest.fn(() => jest.fn()),
}));

jest.mock('components/LoginModal', () => ({
  __esModule: true,
  default: () => null,
}));

const buildResponseError = (status, data = {}) => Object.assign(
  new Error(`Request failed with status code ${status}`),
  { response: { status, data } },
);

describe('urlInterceptor', () => {
  afterEach(() => {
    delete window.CONTEXT_PATH;
  });

  it('prefixes the url with a slash when there is no context path', () => {
    expect(urlInterceptor({ url: 'api/products' }).url).toEqual('/api/products');
  });

  it('prefixes the url with the context path', () => {
    window.CONTEXT_PATH = '/openboxes';

    expect(urlInterceptor({ url: '/api/products' }).url).toEqual('/openboxes/api/products');
  });

  it('does not duplicate slashes around the context path', () => {
    window.CONTEXT_PATH = '/custom/';

    expect(urlInterceptor({ url: '/api/products' }).url).toEqual('/custom/api/products');
  });

  it('removes the legacy hardcoded context path from the url', () => {
    window.CONTEXT_PATH = '/custom';

    expect(urlInterceptor({ url: '/openboxes/api/products' }).url).toEqual('/custom/api/products');
  });

  it('handles a missing url', () => {
    expect(urlInterceptor({}).url).toEqual('/');
  });

  it('keeps the rest of the request config', () => {
    const config = { url: 'api/products', method: 'get', params: { max: 10 } };

    expect(urlInterceptor(config)).toEqual({ ...config, url: '/api/products' });
  });
});

describe('parseResponse', () => {
  it('expands dotted keys into nested objects', () => {
    expect(parseResponse({ 'product.id': '1', 'product.name': 'Aspirin' }))
      .toEqual({ product: { id: '1', name: 'Aspirin' } });
  });

  it('parses objects inside arrays', () => {
    expect(parseResponse([{ 'bin.name': 'A1' }, { 'bin.name': 'A2' }]))
      .toEqual([{ bin: { name: 'A1' } }, { bin: { name: 'A2' } }]);
  });

  it('parses nested values recursively', () => {
    expect(parseResponse({ items: [{ 'lot.number': 'L1' }] }))
      .toEqual({ items: [{ lot: { number: 'L1' } }] });
  });

  it('returns primitive values unchanged', () => {
    expect(parseResponse('text')).toEqual('text');
    expect(parseResponse(5)).toEqual(5);
    expect(parseResponse(null)).toBeNull();
  });
});

describe('mapToEmptyString', () => {
  it('maps empty values to empty strings', () => {
    expect(mapToEmptyString({ city: null, street: undefined, postalCode: '' }))
      .toEqual({ city: '', street: '', postalCode: '' });
  });

  it('keeps filled values', () => {
    expect(mapToEmptyString({ city: 'Boston', number: 5 }))
      .toEqual({ city: 'Boston', number: 5 });
  });
});

describe('handleValidationErrors', () => {
  it('shows the validation messages in an alert for 400 instead of a notification', async () => {
    const setState = jest.fn();
    const error = buildResponseError(400, { errorMessages: ['Name is required', 'Code is required'] });

    await expect(handleValidationErrors(setState)(error)).rejects.toBe(error);

    expect(setState).toHaveBeenCalledWith({
      alertMessage: 'Name is required Code is required',
      showAlert: true,
    });
  });

  it('does not touch the alert for other statuses', async () => {
    const setState = jest.fn();
    const error = buildResponseError(500);

    await expect(handleValidationErrors(setState)(error)).rejects.toBe(error);

    expect(setState).not.toHaveBeenCalled();
  });
});

describe('handlers', () => {
  it('passes a successful response through', () => {
    const response = { status: 200, data: {} };

    expect(handleSuccess(response)).toBe(response);
  });

  it('rejects a request error without handling it', async () => {
    const error = new Error('Request config is invalid');

    await expect(justRejectRequestError(error)).rejects.toBe(error);
  });
});

describe('interceptors', () => {
  it('registers the url interceptor and error handler on apiClient', () => {
    expect(apiClient.interceptors.request.handlers).toEqual([
      expect.objectContaining({ fulfilled: urlInterceptor, rejected: justRejectRequestError }),
    ]);
    expect(apiClient.interceptors.response.handlers).toEqual([
      expect.objectContaining({ fulfilled: handleSuccess, rejected: handleError }),
    ]);
  });

  it('registers only the url interceptor on apiClientCustomResponseHandler', () => {
    expect(apiClientCustomResponseHandler.interceptors.request.handlers).toEqual([
      expect.objectContaining({ fulfilled: urlInterceptor, rejected: justRejectRequestError }),
    ]);
    expect(apiClientCustomResponseHandler.interceptors.response.handlers).toEqual([]);
  });
});
