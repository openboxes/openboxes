import confirmationModal from 'utils/confirmationModalUtils';

/**
 * Warns that some rows could not be saved and that their updates are lost on exit.
 */
const confirmExitWithUnsavedRows = () => new Promise((resolve) => {
  confirmationModal({
    closeOnClickOutside: true,
    hideCloseButton: true,
    handleOnClose: () => resolve(false),
    title: {
      label: 'react.receiving.unsavedRows.confirm.title',
      default: 'Unsaved changes',
    },
    content: {
      label: 'react.receiving.unsavedRows.confirm.message',
      default: 'Some rows were unable to be saved. Do you still want to exit? Updates to those rows will be lost.',
    },
    buttons: (onClose) => [
      {
        variant: 'secondary',
        label: 'react.receiving.exit.label',
        defaultLabel: 'Exit',
        onClick: () => {
          onClose();
          resolve(true);
        },
      },
      {
        variant: 'primary',
        label: 'react.receiving.continueReceipt.label',
        defaultLabel: 'Continue Receipt',
        onClick: () => {
          onClose();
          resolve(false);
        },
      },
    ],
  });
});

export default confirmExitWithUnsavedRows;
