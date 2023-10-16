import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';

const RejectRequestModalButtons = ({ closeRejectionModal }) => (
  <div className="btn-toolbar justify-content-between pt-3">
    <Button
      type="button"
      onClick={closeRejectionModal}
      defaultLabel="Cancel"
      label="react.rejectRequestModal.cancel.label"
    />

    <Button
      type="submit"
      defaultLabel="Confirm reject"
      label="react.rejectRequestModal.confirmReject.label"
    />
  </div>
);

export default RejectRequestModalButtons;

RejectRequestModalButtons.propTypes = {
  closeRejectionModal: PropTypes.func.isRequired,
};
