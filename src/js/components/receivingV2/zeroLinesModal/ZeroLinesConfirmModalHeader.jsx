import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/ri';

const ZeroLinesConfirmModalHeader = ({ title, onClose }) => (
  <div className="d-flex justify-content-between">
    <p className="custom-modal-title">{title}</p>
    <RiCloseFill
      size="32px"
      className="cursor-pointer"
      role="button"
      aria-label="Close modal"
      onClick={onClose}
    />
  </div>
);

ZeroLinesConfirmModalHeader.propTypes = {
  title: PropTypes.string.isRequired,
  onClose: PropTypes.func.isRequired,
};

export default ZeroLinesConfirmModalHeader;
