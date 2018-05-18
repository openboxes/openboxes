import apiClient from '../../utils/apiClient';

describe('apiClient', () => {
  test('returns 200', () => {
    apiClient
      .get('http://httpstat.us/200')
      .then((response) => {
        expect(response.status).toEqual(200);
      })
      .catch(error => error);
  });
  test('returns 400', () => {
    apiClient
      .get('http://httpstat.us/400')
      .then(response => response)
      .catch((error) => {
        expect(error.response.status).toEqual(400);
      });
  });
  test('returns 401', () => {
    apiClient
      .get('http://httpstat.us/401')
      .then(response => response)
      .catch((error) => {
        expect(error.response.status).toEqual(401);
      });
  });
  test('returns 403', () => {
    apiClient
      .get('http://httpstat.us/403')
      .then(response => response)
      .catch((error) => {
        expect(error.response.status).toEqual(403);
      });
  });
  test('returns 404', () => {
    apiClient
      .get('http://httpstat.us/404')
      .then(response => response)
      .catch((error) => {
        expect(error.response.status).toEqual(404);
      });
  });
  test('returns 500', () => {
    apiClient
      .get('http://httpstat.us/500')
      .then(response => response)
      .catch((error) => {
        expect(error.response.status).toEqual(500);
      });
  });
});
