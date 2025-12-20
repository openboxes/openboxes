import React from 'react';

import PropTypes from 'prop-types';
import { RiArrowLeftSLine } from 'react-icons/ri';
import { useHistory } from 'react-router-dom';

import Translate from 'utils/Translate';

import './utils.scss';

/**
 * A button component that redirects to a specified route when clicked.
 * @param className - Additional CSS classes for styling
 * @param label - Translation label ID
 * @param defaultMessage - Default message if translation is not found
 * @param redirectTo - The route to redirect to
 * @param handleOnClick - Optional function that will handle redirect logic,
 * instead of direct redirect
 * @returns {JSX.Element} - The RedirectButton component
 * @constructor
 */
const RedirectButton = ({
  className, label, defaultMessage, redirectTo, handleOnClick,
}) => {
  const history = useHistory();

  return (
    <div
      role="presentation"
      onClick={() => {
        if (handleOnClick) {
          handleOnClick();
          return;
        }
        if (redirectTo) {
          history.push(redirectTo);
        }
      }}
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
  redirectTo: PropTypes.string,
  handleOnClick: PropTypes.func,
};

RedirectButton.defaultProps = {
  className: '',
  handleOnClick: null,
  redirectTo: null,
};
