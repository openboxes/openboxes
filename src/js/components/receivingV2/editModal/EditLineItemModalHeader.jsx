import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/ri';

import useTranslate from 'hooks/useTranslate';

const EditLineItemModalHeader = ({ onClose }) => {
  const translate = useTranslate();

  return (
    <div className="d-flex justify-content-between align-items-center pb-3">
      <h5 className="receiving-edit-modal__title m-0 font-weight-500 font-size-md">
        {translate('react.receiving.editModal.title.label', 'Edit Receiving Information')}
      </h5>
      <RiCloseFill
        size="24px"
        className="cursor-pointer"
        role="button"
        aria-label="Close modal"
        onClick={onClose}
      />
    </div>
  );
};

EditLineItemModalHeader.propTypes = {
  onClose: PropTypes.func.isRequired,
};

export default EditLineItemModalHeader;
