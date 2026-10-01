import React from 'react';

import { confirmAlert } from 'react-confirm-alert';

import ConfirmModal from 'utils/ConfirmModal';

import 'react-confirm-alert/src/react-confirm-alert.css';

/**
 * Convenience wrapper on a {@link ConfirmModal}, wrapping it in a confirmAlert.
 *
 * @param param0
 * @param {object} param0.title - the title text of the modal
 * @param {object} param0.content - the body text of the modal
 * @param {object} param0.buttons - the buttons to include in the modal
 * @param {Function} [param0.handleOnClose] - the function to call when close is triggered
 * @param {boolean} [param0.hideCloseButton] - true if there should NOT be an 'x' button
 * @param {boolean} [param0.closeOnClickOutside=true] - true if clicking outside the window should
 * close the modal
 * @param {boolean} [param0.closeOnEscape=closeOnClickOutside] - true if pressing the escape key
 * should close the modal
 */
const confirmationModal = ({
  title,
  content,
  buttons,
  handleOnClose,
  hideCloseButton,
  closeOnClickOutside = true,
  closeOnEscape = closeOnClickOutside,
}) => {
  confirmAlert({
    customUI: ({ onClose }) => (
      <ConfirmModal
        labels={{
          title,
          content,
        }}
        onClose={() => {
          onClose();
          handleOnClose?.();
        }}
        buttons={buttons(onClose)}
        hideCloseButton={hideCloseButton}
      />
    ),
    closeOnClickOutside,
    closeOnEscape,
    // The library closes the modal itself automatically in these scenarios, so we only need
    // to specify  our custom OnClose logic here.
    onClickOutside: () => handleOnClose?.(),
    onKeypressEscape: () => handleOnClose?.(),
  });
};

export default confirmationModal;
