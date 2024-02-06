import React from 'react';

import PropTypes from 'prop-types';
import { RiArrowLeftSLine } from 'react-icons/ri';
import { useHistory } from 'react-router-dom';

import Translate from 'utils/Translate';

import './utils.scss';

const RedirectButton = ({
  className, label, defaultMessage, redirectTo,
}) => {
  const history = useHistory();

  return (
    <div
      role="presentation"
      onClick={() => history.push(redirectTo)}
      className={`redirect-button d-flex align-items-center ${className}`}
    >
      <span>
        <RiArrowLeftSLine />
        <Translate
          id={label}
          defaultMessage={defaultMessage}
        />
      </span>
    </div>
  );
};

export default RedirectButton;

RedirectButton.propTypes = {
  className: PropTypes.string,
  label: PropTypes.string.isRequired,
  defaultMessage: PropTypes.string.isRequired,
  redirectTo: PropTypes.string.isRequired,
};

RedirectButton.defaultProps = {
  className: '',
};
