import React from 'react';

import PropTypes from 'prop-types';

import SaveStatus from 'consts/saveStatus';

const SaveIndicator = ({ lineStatus }) => <div className={`${lineStatus?.toLowerCase()} indicator`} />;

export default SaveIndicator;

SaveIndicator.propTypes = {
  lineStatus: PropTypes.string,
};

SaveIndicator.defaultProps = {
  // Fetched items have to be already saved,
  // so I am setting SaveStatus.SAVED as a default
  lineStatus: SaveStatus.SAVED,
};
