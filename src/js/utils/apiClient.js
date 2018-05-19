/* eslint-disable no-console */
import axios from 'axios';

const justRejectRequestError = error => Promise.reject(error);

const apiClient = axios.create({});

const handleSuccess = response => response;

const handleError = (error) => {
  switch (error.response.status) {
    case 400:
      console.error('Bad request.');
      break;
    case 401:
      console.error('Unauthorized.');
      break;
    case 403:
      console.error('Access denied.');
      break;
    case 404:
      console.error('Not found.');
      break;
    case 500:
      console.error('Internal server error.');
      break;
    default: {
      console.error(error);
    }
  }
  return Promise.reject(error);
};

apiClient.interceptors.response.use(handleSuccess, handleError);
apiClient.interceptors.request.use(config => config, justRejectRequestError);

export default apiClient;
