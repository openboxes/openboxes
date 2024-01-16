import React from 'react';

import { confirmAlert } from 'react-confirm-alert';

import CustomConfirmModal from 'utils/CustomConfirmModal';

import 'react-confirm-alert/src/react-confirm-alert.css';

const confirmationModal = ({ title, content, buttons }) => {
  confirmAlert({
    customUI: ({ onClose }) => (
      <CustomConfirmModal
        labels={{
          title,
          content,
        }}
        onClose={onClose}
        buttons={buttons(onClose)}
      />
    ),
  });
};

export default confirmationModal;
