import { useCallback, useMemo } from 'react';

import confirmationModal from 'utils/confirmationModalUtils';

/**
 * Hook to show a confirmation modal when there are unsaved changes.
 * @param onConfirm - Function to call when the user confirms navigation away
 * @returns {(function(): void)} - Function to open the confirmation modal
 */
const useUnsavedChangesConfirmationModal = ({ onConfirm }) => {
  const modalLabels = useMemo(() => ({
    title: {
      label: 'react.default.unsavedChangesConfirmationModal.title',
      default: 'Are you sure?',
    },
    content: {
      label: 'react.default.unsavedChangesConfirmationModal.content',
      default: 'Are you sure you want to go back without saving?',
    },
  }), []);

  const modalButtons = useCallback((onClose) => ([
    {
      variant: 'transparent',
      defaultLabel: 'Cancel',
      label: 'react.default.unsavedChangesConfirmationModal.cancel',
      onClick: () => {
        onClose?.();
      },
    },
    {
      variant: 'primary',
      defaultLabel: 'Confirm',
      label: 'react.default.unsavedChangesConfirmationModal.confirm',
      onClick: () => {
        onConfirm?.();
        onClose?.();
      },
    },
  ]), [onConfirm]);

  return useCallback(
    () => {
      confirmationModal({
        buttons: modalButtons,
        ...modalLabels,
        hideCloseButton: true,
        closeOnClickOutside: false,
      });
    }, [modalButtons, modalLabels],
  );
};

export default useUnsavedChangesConfirmationModal;
