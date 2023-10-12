import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const RejectRequestModalHeader = ({ id }) => (
  <div className="header">
    <h4 className="title">
      <Translate
        id="react.autosaveFeatureModal.title.label"
        defaultMessage={`Please provide a reason for rejecting request: ${id}`}
      />
    </h4>
  </div>
);

export default RejectRequestModalHeader;

RejectRequestModalHeader.propTypes = {
  id: PropTypes.string.isRequired,
};
