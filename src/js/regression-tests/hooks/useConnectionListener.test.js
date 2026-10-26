import { renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';

import { SET_OFFLINE, SET_ONLINE } from 'actions/types';
import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import useConnectionListener from 'hooks/useConnectionListener';

const mockShowNotification = jest.fn();
jest.mock('components/Layout/notifications/notification', () => jest.fn(() => mockShowNotification));
jest.mock('react-redux', () => ({
  useDispatch: jest.fn(),
  useSelector: jest.fn(),
  connect: jest.fn(() => (Component) => Component),
}));

const CONNECTION_TIMEOUT = 3000;

describe('useConnectionListener', () => {
  const dispatch = jest.fn();
  let online;
  let navigatorOnline;

  const goOnline = () => {
    navigatorOnline = true;
    window.dispatchEvent(new Event('online'));
  };

  const goOffline = () => {
    navigatorOnline = false;
    window.dispatchEvent(new Event('offline'));
  };

  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
    online = true;
    navigatorOnline = true;
    jest.spyOn(navigator, 'onLine', 'get').mockImplementation(() => navigatorOnline);
    useDispatch.mockReturnValue(dispatch);
    useSelector.mockImplementation((selector) => selector({
      connection: { online },
      session: { browserConnectionTimeout: CONNECTION_TIMEOUT },
    }));
  });

  afterEach(() => {
    jest.useRealTimers();
    jest.restoreAllMocks();
  });

  it('marks the app offline as soon as the browser loses the connection', () => {
    renderHook(() => useConnectionListener());

    goOffline();

    expect(dispatch).toHaveBeenCalledWith({ type: SET_OFFLINE });
    expect(notification).toHaveBeenCalledWith(NotificationType.INFO);
    expect(mockShowNotification).toHaveBeenCalledWith(expect.objectContaining({
      message: 'Lost Connection',
      details: 'You are now offline. Your changes will be saved when the connection is restored.',
    }));
  });

  it('marks the app online only once the connection has held for the timeout', () => {
    online = false;
    navigatorOnline = false;
    renderHook(() => useConnectionListener());

    goOnline();
    jest.advanceTimersByTime(CONNECTION_TIMEOUT - 1);
    expect(dispatch).not.toHaveBeenCalled();

    jest.advanceTimersByTime(1);
    expect(dispatch).toHaveBeenCalledWith({ type: SET_ONLINE });
    expect(notification).toHaveBeenCalledWith(NotificationType.SUCCESS);
    expect(mockShowNotification).toHaveBeenCalledWith(expect.objectContaining({
      message: 'Connection restored.',
      details: 'You are back online. You can now continue your work.',
    }));
  });

  it('stays offline when the connection drops again before the timeout', () => {
    online = false;
    navigatorOnline = false;
    renderHook(() => useConnectionListener());

    goOnline();
    jest.advanceTimersByTime(1000);
    goOffline();
    jest.advanceTimersByTime(CONNECTION_TIMEOUT);

    // Already offline: neither the drop nor the cancelled reconnection change anything
    expect(dispatch).not.toHaveBeenCalled();
    expect(notification).not.toHaveBeenCalled();
  });

  it('waits for a single timeout on repeated online events', () => {
    online = false;
    navigatorOnline = false;
    renderHook(() => useConnectionListener());

    goOnline();
    jest.advanceTimersByTime(2000);
    goOnline();
    jest.advanceTimersByTime(1000);

    expect(dispatch).toHaveBeenCalledTimes(1);
    expect(dispatch).toHaveBeenCalledWith({ type: SET_ONLINE });
  });

  it('does nothing when the status did not change', () => {
    renderHook(() => useConnectionListener());

    goOnline();
    jest.advanceTimersByTime(CONNECTION_TIMEOUT);

    expect(dispatch).not.toHaveBeenCalled();
  });

  it('stops listening and drops a pending reconnection when unmounted', () => {
    online = false;
    navigatorOnline = false;
    const { unmount } = renderHook(() => useConnectionListener());
    goOnline();

    unmount();
    jest.advanceTimersByTime(CONNECTION_TIMEOUT);
    expect(dispatch).not.toHaveBeenCalled();

    // A listener left behind would start a new reconnection here.
    goOnline();
    jest.advanceTimersByTime(CONNECTION_TIMEOUT);
    expect(dispatch).not.toHaveBeenCalled();
  });

  it('stops reacting to a lost connection when unmounted', () => {
    const { unmount } = renderHook(() => useConnectionListener());

    unmount();
    goOffline();

    expect(dispatch).not.toHaveBeenCalled();
  });
});
