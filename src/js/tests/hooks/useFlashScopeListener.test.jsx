import React from 'react';

import { renderHook } from '@testing-library/react-hooks';
import { MemoryRouter } from 'react-router-dom';

import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import useFlashScopeListener from 'hooks/useFlashScopeListener';

const mockShowNotification = jest.fn();

jest.mock('components/Layout/notifications/notification', () => ({
  __esModule: true,
  default: jest.fn(() => mockShowNotification),
}));

const renderWithFlash = (flash) => {
  const search = flash ? `?flash=${encodeURIComponent(JSON.stringify(flash))}` : '';
  return renderHook(() => useFlashScopeListener(), {
    wrapper: ({ children }) => (
      <MemoryRouter initialEntries={[`/openboxes/${search}`]}>{children}</MemoryRouter>
    ),
  });
};

describe('useFlashScopeListener', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('shows a flash message as a success notification', () => {
    renderWithFlash({ message: 'Your error has been reported' });

    expect(notification).toHaveBeenCalledWith(NotificationType.SUCCESS);
    expect(mockShowNotification).toHaveBeenCalledWith({ message: 'Your error has been reported' });
  });

  it('shows a flash error as an error notification with its text', () => {
    renderWithFlash({ error: 'Email was NOT sent' });

    expect(notification).toHaveBeenCalledWith(NotificationType.ERROR_OUTLINED);
    expect(mockShowNotification).toHaveBeenCalledWith({ message: 'Email was NOT sent' });
  });

  it('shows nothing without a flash parameter', () => {
    renderWithFlash(null);

    expect(notification).not.toHaveBeenCalled();
  });
});
