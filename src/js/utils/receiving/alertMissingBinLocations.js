import confirmationModal from 'utils/confirmationModalUtils';

/**
 * Blocks the transition to the check step, telling the user to fill the putaway location in. The
 * location cannot be entered on the check step anymore, and completing a receipt with a line that
 * received something into no bin is refused by the backend
 * (ReceiptCompleteRequestCommandValidator), so it has to be entered here.
 *
 * @param linesCount How many lines being received have no location.
 */
const alertMissingBinLocations = (linesCount) => {
  confirmationModal({
    closeOnClickOutside: false,
    title: {
      label: 'react.receiving.missingLocation.title',
      default: 'Missing location',
    },
    content: {
      label: 'react.receiving.missingLocation.message',
      default: `You have not entered a location for ${linesCount} line/lines you are receiving. `
        + 'Enter a location for every line before continuing.',
      data: { count: linesCount },
    },
    buttons: (onClose) => [
      {
        variant: 'primary',
        label: 'react.default.ok.label',
        defaultLabel: 'Ok',
        onClick: onClose,
      },
    ],
  });
};

export default alertMissingBinLocations;
