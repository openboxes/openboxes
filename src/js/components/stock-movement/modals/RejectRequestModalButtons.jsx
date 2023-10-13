import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const RejectRequestModalButtons = ({ closeRejectionModal }) => (
  <div className="btn-toolbar justify-content-between pt-3">
    <button
      type="button"
      className="btn btn-outline-primary ml-1"
      onClick={closeRejectionModal}
    >
      <Translate id="react.rejectRequestModal.cancel.label" defaultMessage="Cancel" />
    </button>
    <button
      type="submit"
      className="btn btn-primary align-self-end"
    >
      <Translate id="react.rejectRequestModal.confirmReject.label" defaultMessage="Confirm reject" />
    </button>
  </div>
);

export default RejectRequestModalButtons;

RejectRequestModalButtons.propTypes = {
  closeRejectionModal: PropTypes.func.isRequired,
};
