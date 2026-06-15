import React from 'react';

import PropTypes from 'prop-types';

import { AutosaveConfig, AutosaveStatus } from 'consts/autosaveStatuses';
import Translate from 'utils/Translate';

import 'utils/AutosaveIndicator.scss';

/**
 * Component that reflects the current autosave state.
 * The state to render is based on the `status` prop, which
 * must be one of the AutosaveStatus consts (saved, saving, error).
 */
const AutosaveIndicator = ({ status }) => {
  const config = AutosaveConfig[status];

  if (!config) {
    return null;
  }

  return (
    <div className={`autosave-indicator autosave-indicator--${status}`}>
      <span className="autosave-indicator__icon">{config.icon}</span>
      <span className="autosave-indicator__label">
        <Translate id={config.label} defaultMessage={config.defaultLabel} />
      </span>
    </div>
  );
};

export default AutosaveIndicator;

AutosaveIndicator.propTypes = {
  status: PropTypes.oneOf(
    Object.values(AutosaveStatus),
  ).isRequired,
};
