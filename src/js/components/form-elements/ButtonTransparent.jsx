import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const ButtonTransparent = ({ label, defaultLabel, onClickAction }) => (
  <button
    className="transparent-button d-flex justify-content-center align-items-center"
    type="button"
    onClick={onClickAction}
  >
    <Translate id={label} defaultMessage={defaultLabel} />
  </button>
);

export default ButtonTransparent;

ButtonTransparent.propTypes = {
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  onClickAction: PropTypes.func,
};

ButtonTransparent.defaultProps = {
  onClickAction: undefined,
};
