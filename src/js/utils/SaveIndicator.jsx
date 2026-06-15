import React from 'react';

import PropTypes from 'prop-types';

import { AutosaveStatus } from 'consts/autosaveStatuses';

import 'utils/SaveIndicator.scss';

/**
 * Row-level indicator rendered inside table cells as a full-height green stripe
 */
const SaveIndicator = ({ status }) => {
  if (status !== AutosaveStatus.SAVED) {
    return null;
  }

  return <span className="save-indicator" aria-hidden />;
};

export default SaveIndicator;

SaveIndicator.propTypes = {
  status: PropTypes.oneOf(Object.values(AutosaveStatus)).isRequired,
};
