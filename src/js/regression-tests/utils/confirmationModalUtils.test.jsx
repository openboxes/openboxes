import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { confirmAlert } from 'react-confirm-alert';

import confirmationModal from 'utils/confirmationModalUtils';

import '@testing-library/jest-dom';

// Not covered on OBAF (OBAF lacks the code: no closeOnEscape option and no
// onClickOutside / onKeypressEscape handlers): 'closes on escape only when it closes on a click
// outside, unless told otherwise', 'runs handleOnClose when the library closes the modal on a
// click outside or on escape', 'ignores a click outside or escape without a handleOnClose', and
// the closeOnEscape assertion of 'closes on a click outside and on escape by default' (OBAF
// leaves escape to react-confirm-alert's own default).

jest.mock('react-confirm-alert', () => ({ confirmAlert: jest.fn() }));
// The real modal needs the translation store; this stand-in shows what it was given.
// eslint-disable-next-line react/prop-types
jest.mock('utils/ConfirmModal', () => ({
  labels, onClose, buttons, hideCloseButton,
}) => (
  <div>
    <p>{labels.title.default}</p>
    <p>{labels.content.default}</p>
    {!hideCloseButton && <button type="button" onClick={onClose}>Close</button>}
    {buttons.map((button) => (
      <button key={button.label} type="button" onClick={button.onClick}>{button.defaultLabel}</button>
    ))}
  </div>
));

const title = { label: 'react.test.title', default: 'Discard changes?' };
const content = { label: 'react.test.content', default: 'Your edits will be lost.' };

const open = (options = {}) => {
  const buttons = jest.fn((onClose) => [
    { label: 'react.test.ok', defaultLabel: 'OK', onClick: onClose },
  ]);
  confirmationModal({
    title, content, buttons, ...options,
  });
  return { alertOptions: confirmAlert.mock.calls[0][0], buttons };
};

describe('confirmationModal()', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders the title, the content and the buttons built with the close function', () => {
    const { alertOptions, buttons } = open();
    const onClose = jest.fn();

    render(alertOptions.customUI({ onClose }));

    expect(screen.getByText('Discard changes?')).toBeInTheDocument();
    expect(screen.getByText('Your edits will be lost.')).toBeInTheDocument();
    expect(buttons).toHaveBeenCalledWith(onClose);
    fireEvent.click(screen.getByRole('button', { name: 'OK' }));
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('closes the modal and runs handleOnClose from the close button', () => {
    const handleOnClose = jest.fn();
    const { alertOptions } = open({ handleOnClose });
    const onClose = jest.fn();
    render(alertOptions.customUI({ onClose }));

    fireEvent.click(screen.getByRole('button', { name: 'Close' }));

    expect(onClose).toHaveBeenCalledTimes(1);
    expect(handleOnClose).toHaveBeenCalledTimes(1);
  });

  it('closes the modal from the close button without a handleOnClose', () => {
    const { alertOptions } = open();
    const onClose = jest.fn();
    render(alertOptions.customUI({ onClose }));

    fireEvent.click(screen.getByRole('button', { name: 'Close' }));

    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('hides the close button when asked to', () => {
    const { alertOptions } = open({ hideCloseButton: true });
    render(alertOptions.customUI({ onClose: jest.fn() }));

    expect(screen.queryByRole('button', { name: 'Close' })).not.toBeInTheDocument();
  });

  it('closes on a click outside and on escape by default', () => {
    const { alertOptions } = open();

    expect(alertOptions.closeOnClickOutside).toBe(true);
  });
});
