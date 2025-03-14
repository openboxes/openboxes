import React from 'react';

import { confirmAlert } from 'react-confirm-alert';

import ConfirmModal from 'utils/ConfirmModal';

import 'react-confirm-alert/src/react-confirm-alert.css';

const confirmationModal = ({
  title,
  content,
  buttons,
  handleOnClose,
  hideCloseButton,
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
  });
};

export default confirmationModal;
