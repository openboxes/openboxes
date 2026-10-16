// Factory for jest.mock('utils/apiClient', ...): both axios instances, every HTTP method mocked.
// The default export is itself callable, like an axios instance.
const HTTP_METHODS = ['get', 'post', 'put', 'patch', 'delete'];

const mockAxiosInstance = () => {
  const instance = jest.fn();
  HTTP_METHODS.forEach((method) => {
    instance[method] = jest.fn();
  });
  return instance;
};

const mockApiClientModule = () => ({
  __esModule: true,
  default: mockAxiosInstance(),
  apiClientCustomResponseHandler: mockAxiosInstance(),
});

export default mockApiClientModule;
