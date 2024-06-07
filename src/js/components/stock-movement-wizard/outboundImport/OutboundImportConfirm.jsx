import React from 'react';

import PropTypes from 'prop-types';

const OutboundImportConfirm = ({ previous }) => (
  <div>
    Confirm
    <button type="button" onClick={() => previous()}>previous</button>
  </div>
);

export default OutboundImportConfirm;

OutboundImportConfirm.propTypes = {
  previous: PropTypes.func.isRequired,
};
