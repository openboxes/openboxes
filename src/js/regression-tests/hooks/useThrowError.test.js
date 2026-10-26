import { renderHook } from '@testing-library/react-hooks';

import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import useThrowError from 'hooks/useThrowError';

const mockShowNotification = jest.fn();
jest.mock('components/Layout/notifications/notification', () => jest.fn(() => mockShowNotification));

describe('useThrowError', () => {
  const errorProps = {
    errorMessageLabel: 'react.cycleCount.noItemsSelected.label',
    errorMessageDefault: 'Select at least {count} item',
    translateData: { count: 1 },
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('runs the action when the condition holds', async () => {
    const callWhenValid = jest.fn().mockResolvedValue();
    const { result } = renderHook(() => useThrowError({
      ...errorProps, condition: true, callWhenValid,
    }));

    await result.current.verifyCondition();

    expect(callWhenValid).toHaveBeenCalledTimes(1);
    expect(notification).not.toHaveBeenCalled();
  });

  it('shows the translated error instead of running the action', async () => {
    const callWhenValid = jest.fn();
    const { result } = renderHook(() => useThrowError({
      ...errorProps, condition: false, callWhenValid,
    }));

    await result.current.verifyCondition();

    expect(callWhenValid).not.toHaveBeenCalled();
    expect(notification).toHaveBeenCalledWith(NotificationType.ERROR_OUTLINED);
    const { message } = mockShowNotification.mock.calls[0][0];
    expect(message.props).toMatchObject({
      id: 'react.cycleCount.noItemsSelected.label',
      data: { count: 1 },
    });
    expect(message.props.options.onMissingTranslation()).toBe('Select at least {count} item');
  });

  it('waits for the action to finish', async () => {
    let finish;
    const callWhenValid = jest.fn(() => new Promise((resolve) => { finish = resolve; }));
    const { result } = renderHook(() => useThrowError({
      ...errorProps, condition: 1, callWhenValid,
    }));

    let done = false;
    const verifying = result.current.verifyCondition().then(() => { done = true; });
    await Promise.resolve();
    expect(done).toBe(false);

    finish();
    await verifying;
    expect(done).toBe(true);
  });

  it('passes on the failure of the action', async () => {
    const callWhenValid = jest.fn().mockRejectedValue(new Error('Request failed'));
    const { result } = renderHook(() => useThrowError({
      ...errorProps, condition: true, callWhenValid,
    }));

    await expect(result.current.verifyCondition()).rejects.toThrow('Request failed');
  });
});
