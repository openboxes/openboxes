import { AxiosError, CanceledError, CancelToken } from 'axios';
import { confirmAlert } from 'react-confirm-alert';

import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import { handleError } from 'utils/apiClient';

const mockShowNotification = jest.fn();

jest.mock('components/Layout/notifications/notification', () => ({
  __esModule: true,
  default: jest.fn(() => mockShowNotification),
}));

jest.mock('react-confirm-alert', () => ({
  confirmAlert: jest.fn(),
}));

jest.mock('components/LoginModal', () => ({
  __esModule: true,
  default: () => null,
}));

const buildResponseError = (status, data = {}) => Object.assign(
  new Error(`Request failed with status code ${status}`),
  { response: { status, data } },
);

describe('handleError', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('canceled requests', () => {
    it('does not notify when a pending request is replaced by a new one', async () => {
      const source = CancelToken.source();
      source.cancel('Cancelled due to new request');

      await expect(handleError(source.token.reason)).rejects.toBe(source.token.reason);

      expect(notification).not.toHaveBeenCalled();
      expect(mockShowNotification).not.toHaveBeenCalled();
    });

    it('does not notify when fetching is canceled on unmount', async () => {
      const error = new CanceledError('Fetching canceled');

      await expect(handleError(error)).rejects.toBe(error);

      expect(notification).not.toHaveBeenCalled();
      expect(mockShowNotification).not.toHaveBeenCalled();
    });
  });

  describe('network errors', () => {
    it('does not notify, because the lost connection indicator covers it', async () => {
      const error = Object.assign(new Error('Network Error'), {
        code: AxiosError.ERR_NETWORK,
      });

      await expect(handleError(error)).rejects.toBe(error);

      expect(notification).not.toHaveBeenCalled();
      expect(mockShowNotification).not.toHaveBeenCalled();
    });
  });

  describe('responses with a status', () => {
    it('notifies about a bad request with errorMessages for 400', async () => {
      const error = buildResponseError(400, {
        errorMessages: ['Quantity is required', 'Lot number is required'],
      });

      await expect(handleError(error)).rejects.toBe(error);

      expect(notification).toHaveBeenCalledWith(NotificationType.ERROR_OUTLINED);
      expect(mockShowNotification).toHaveBeenCalledWith({
        message: 'Bad request',
        details: 'Quantity is required, Lot number is required',
      });
    });

    it('falls back to errorMessage for 400 when errorMessages is missing', async () => {
      const error = buildResponseError(400, { errorMessage: 'Payload is not valid' });

      await expect(handleError(error)).rejects.toBe(error);

      expect(mockShowNotification).toHaveBeenCalledWith({
        message: 'Bad request',
        details: 'Payload is not valid',
      });
    });

    it('opens the login modal for 401 instead of notifying', async () => {
      const error = buildResponseError(401);

      await expect(handleError(error)).rejects.toBe(error);

      expect(confirmAlert).toHaveBeenCalledWith(
        expect.objectContaining({ customUI: expect.any(Function) }),
      );
      expect(notification).not.toHaveBeenCalled();
    });

    it('notifies about a missing resource for 404', async () => {
      const error = buildResponseError(404, { errorMessage: 'Shipment not found' });

      await expect(handleError(error)).rejects.toBe(error);

      expect(notification).toHaveBeenCalledWith(NotificationType.ERROR_OUTLINED);
      expect(mockShowNotification).toHaveBeenCalledWith({
        message: 'Not found',
        details: 'Shipment not found',
      });
    });

    it('notifies about an internal server error for 500', async () => {
      const error = buildResponseError(500, { errorMessages: ['Unexpected failure'] });

      await expect(handleError(error)).rejects.toBe(error);

      expect(notification).toHaveBeenCalledWith(NotificationType.ERROR_FILLED);
      expect(mockShowNotification).toHaveBeenCalledWith({
        message: 'Internal server error',
        details: 'Unexpected failure',
      });
    });

    it('notifies with the raw error message for an unhandled status', async () => {
      const error = buildResponseError(409, { errorMessage: 'Version conflict' });

      await expect(handleError(error)).rejects.toBe(error);

      expect(notification).toHaveBeenCalledWith(NotificationType.ERROR_FILLED);
      expect(mockShowNotification).toHaveBeenCalledWith({
        message: 'Request failed with status code 409',
        details: 'Version conflict',
      });
    });
  });

  describe('errors without a response', () => {
    it('notifies with the error message when the request never got a response', async () => {
      const error = Object.assign(new Error('timeout of 0ms exceeded'), {
        code: AxiosError.ECONNABORTED,
      });

      await expect(handleError(error)).rejects.toBe(error);

      expect(notification).toHaveBeenCalledWith(NotificationType.ERROR_FILLED);
      expect(mockShowNotification).toHaveBeenCalledWith({
        message: 'timeout of 0ms exceeded',
        details: '',
      });
    });
  });
});
