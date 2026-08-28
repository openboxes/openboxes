import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';

const ZeroLinesConfirmModalFooter = ({ onConfirm, onCancel }) => (
  <div className="d-flex justify-content-end mt-4">
    <Button
      label="react.default.no.label"
      defaultLabel="No"
      variant="secondary"
      onClick={onCancel}
    />
    <Button
      label="react.default.yes.label"
      defaultLabel="Yes"
      variant="primary"
      onClick={onConfirm}
    />
  </div>
);

ZeroLinesConfirmModalFooter.propTypes = {
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
};

export default ZeroLinesConfirmModalFooter;
