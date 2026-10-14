import { renderHook } from '@testing-library/react-hooks';

import useUnsavedChangesConfirmationModal from 'hooks/useUnsavedChangesConfirmationModal';
import confirmationModal from 'utils/confirmationModalUtils';

jest.mock('utils/confirmationModalUtils', () => jest.fn());

describe('useUnsavedChangesConfirmationModal', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const openModal = (onConfirm) => {
    const { result } = renderHook(() => useUnsavedChangesConfirmationModal({ onConfirm }));
    result.current();
    return confirmationModal.mock.calls[0][0];
  };

  it('asks for confirmation in a modal that only its buttons close', () => {
    const modal = openModal(jest.fn());

    expect(modal).toMatchObject({
      title: {
        label: 'react.default.unsavedChangesConfirmationModal.title',
        default: 'Are you sure?',
      },
      content: {
        label: 'react.default.unsavedChangesConfirmationModal.content',
        default: 'Are you sure you want to go back without saving?',
      },
      hideCloseButton: true,
      closeOnClickOutside: false,
    });
    expect(modal.buttons(jest.fn()).map(({ defaultLabel, variant }) => [defaultLabel, variant]))
      .toEqual([['Cancel', 'transparent'], ['Confirm', 'primary']]);
  });

  it('closes on cancel without leaving', () => {
    const onConfirm = jest.fn();
    const onClose = jest.fn();
    const [cancel] = openModal(onConfirm).buttons(onClose);

    cancel.onClick();

    expect(onClose).toHaveBeenCalledTimes(1);
    expect(onConfirm).not.toHaveBeenCalled();
  });

  it('leaves and closes on confirm', () => {
    const onConfirm = jest.fn();
    const onClose = jest.fn();
    const [, confirm] = openModal(onConfirm).buttons(onClose);

    confirm.onClick();

    expect(onConfirm).toHaveBeenCalledTimes(1);
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('closes on confirm when there is nothing to run', () => {
    const onClose = jest.fn();
    const [, confirm] = openModal(undefined).buttons(onClose);

    expect(() => confirm.onClick()).not.toThrow();
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('keeps the same opener between renders while the action is the same', () => {
    const onConfirm = jest.fn();
    const { result, rerender } = renderHook(
      (props) => useUnsavedChangesConfirmationModal(props),
      { initialProps: { onConfirm } },
    );
    const firstOpener = result.current;

    rerender({ onConfirm });
    expect(result.current).toBe(firstOpener);

    rerender({ onConfirm: jest.fn() });
    expect(result.current).not.toBe(firstOpener);
  });
});
