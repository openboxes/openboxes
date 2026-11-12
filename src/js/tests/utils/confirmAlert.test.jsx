import React from 'react';

import {
  act, fireEvent, screen, waitFor,
} from '@testing-library/react';
import { confirmAlert } from 'react-confirm-alert';

const openAlert = (options) => act(() => {
  confirmAlert(options);
});

describe('confirmAlert', () => {
  afterEach(() => {
    document.body.innerHTML = '';
  });

  it('renders a dialog with buttons and closes it after a click', async () => {
    const onConfirm = jest.fn();

    await openAlert({
      title: 'Confirm refresh',
      message: 'Unsaved changes will be lost',
      buttons: [{ label: 'Yes', onClick: onConfirm }, { label: 'No' }],
    });

    expect(screen.getByText('Confirm refresh')).toBeTruthy();
    expect(document.querySelector('.react-confirm-alert-overlay .react-confirm-alert-body')).toBeTruthy();

    await act(() => fireEvent.click(screen.getByText('Yes')));

    expect(onConfirm).toHaveBeenCalled();
    await waitFor(() => expect(screen.queryByText('Confirm refresh')).toBeNull());
  });

  it('renders a custom UI and closes it with onClose', async () => {
    await openAlert({
      customUI: ({ onClose }) => (
        <button type="button" onClick={onClose}>Close login modal</button>
      ),
    });

    await act(() => fireEvent.click(screen.getByText('Close login modal')));

    await waitFor(() => expect(screen.queryByText('Close login modal')).toBeNull());
  });

  it('can be opened again after closing', async () => {
    await openAlert({ title: 'First', buttons: [{ label: 'OK' }] });
    await act(() => fireEvent.click(screen.getByText('OK')));
    await openAlert({ title: 'Second', buttons: [{ label: 'OK' }] });

    expect(screen.queryByText('First')).toBeNull();
    expect(screen.getByText('Second')).toBeTruthy();
  });
});
