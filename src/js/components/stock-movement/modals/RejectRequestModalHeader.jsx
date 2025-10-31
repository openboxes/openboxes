import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const RejectRequestModalHeader = ({ identifier }) => (
  <div className="header">
    <h4 className="title">
      <Translate
        id="react.rejectRequestModal.provideReason.label"
        defaultMessage="Please provide a reason for rejecting request"
      />
      :
      {identifier}
    </h4>
  </div>
);

export default RejectRequestModalHeader;

RejectRequestModalHeader.propTypes = {
  identifier: PropTypes.string.isRequired,
};
