import React from 'react';

import { confirmAlert } from 'react-confirm-alert';

import ZeroLinesConfirmModal from 'components/receivingV2/zeroLinesModal/ZeroLinesConfirmModal';

import 'react-confirm-alert/src/react-confirm-alert.css';

/**
 * Warns that the lines left blank will be received as zero, listing them. Resolves to true only
 * when the user picks "Yes" - closing the modal any other way keeps them on the receiving step.
 *
 * @param blankRows Rows with no quantity entered, listed in the modal.
 * @param translate Translation function, read from the app tree the modal is rendered outside of.
 * @param localeKey Locale code the expiration dates are formatted with.
 */
const confirmBlankLinesAsZero = ({ blankRows, translate, localeKey }) => new Promise((resolve) => {
  confirmAlert({
    closeOnClickOutside: false,
    afterClose: () => resolve(false),
    customUI: ({ onClose }) => (
      <ZeroLinesConfirmModal
        lines={blankRows}
        translate={translate}
        localeKey={localeKey}
        onConfirm={() => {
          resolve(true);
          onClose();
        }}
        onCancel={() => {
          resolve(false);
          onClose();
        }}
      />
    ),
  });
});

export default confirmBlankLinesAsZero;
