/**
 * Factory for jest.mock('utils/apiClient', ...): one client mock for both axios instances; the
 * helpers come from the real module, loaded on first use (loading it eagerly would import the
 * store before the reducers).
 */
const apiClientModule = () => {
  const client = jest.fn();
  ['get', 'post', 'put', 'patch', 'delete'].forEach((method) => {
    client[method] = jest.fn();
  });
  client.interceptors = { response: { use: jest.fn() } };
  const actual = (name) => (...args) => jest.requireActual('utils/apiClient')[name](...args);
  return {
    __esModule: true,
    default: client,
    apiClientCustomResponseHandler: client,
    parseResponse: actual('parseResponse'),
    handleError: actual('handleError'),
    handleSuccess: actual('handleSuccess'),
    handleValidationErrors: actual('handleValidationErrors'),
    mapToEmptyString: actual('mapToEmptyString'),
    justRejectRequestError: actual('justRejectRequestError'),
  };
};

export default apiClientModule;
