import confirmationModal from 'utils/confirmationModalUtils';

/**
 * Warns that autofilling the putaway location will overwrite locations the user has
 * already entered. Runs onConfirm only when the user picks "Yes".
 */
const confirmLocationAutofillOverwrite = (onConfirm) => {
  confirmationModal({
    title: {
      label: 'react.receiving.autofillLocation.confirm.title',
      default: 'Autofill location',
    },
    content: {
      label: 'react.receiving.autofillLocation.confirm.message',
      default: 'Autofilling the location will overwrite any location values that you have already entered. Do you want to proceed?',
    },
    buttons: (onClose) => [
      {
        variant: 'secondary',
        label: 'react.default.no.label',
        defaultLabel: 'No',
        onClick: onClose,
      },
      {
        variant: 'primary',
        label: 'react.default.yes.label',
        defaultLabel: 'Yes',
        onClick: () => {
          onClose();
          onConfirm();
        },
      },
    ],
  });
};

export default confirmLocationAutofillOverwrite;
