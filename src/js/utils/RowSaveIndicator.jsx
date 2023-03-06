import React from 'react';

import PropTypes from 'prop-types';

import RowSaveStatus from 'consts/rowSaveStatus';

const RowSaveIndicator = ({ lineItemSaveStatus }) =>
  <div className={`${lineItemSaveStatus?.toLowerCase()} line-item-save-indicator`} />;

export default RowSaveIndicator;

RowSaveIndicator.propTypes = {
  lineItemSaveStatus: PropTypes.string,
};

RowSaveIndicator.defaultProps = {
  // Fetched items have to be already saved,
  // so I am setting SaveStatus.SAVED as a default
  lineItemSaveStatus: RowSaveStatus.SAVED,
};
