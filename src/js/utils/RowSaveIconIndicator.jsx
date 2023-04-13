import React from 'react';

import PropTypes from 'prop-types';

import RowSaveStatus from 'consts/rowSaveStatus';

const RowSaveIconIndicator = ({ fieldConfig: { lineItemSaveStatus } }) => {
  return <div>{lineItemSaveStatus}</div>;
}

export default RowSaveIconIndicator;

RowSaveIconIndicator.propTypes = {
  fieldConfig: PropTypes.shape({
    lineItemSaveStatus: PropTypes.string,
  }),
};

RowSaveIconIndicator.defaultProps = {
  fieldConfig: PropTypes.shape({
    lineItemSaveStatus: RowSaveStatus.SAVED,
  }),
};

