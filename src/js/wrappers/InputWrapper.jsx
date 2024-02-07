import React from 'react';

import PropTypes from 'prop-types';
import { RiQuestionLine } from 'react-icons/ri';
import { Tooltip } from 'react-tippy';

import Translate from 'utils/Translate';

import './style.scss';

const InputWrapper = ({
  children,
  tooltip,
  required,
  button,
  title,
  errorMessage,
}) => (
  <div className="input-wrapper-container">
    <div>
      <div className="input-wrapper-title">
        {title && <Translate id={title?.id} defaultMessage={title?.defaultMessage} />}
        {tooltip && (
        <Tooltip
          html={(
            <span className="p-1">
              <Translate id={tooltip.id} defaultMessage={tooltip.defaultMessage} />
            </span>
              )}
        >
          <RiQuestionLine />
        </Tooltip>
        )}
        {required && <span className="input-wrapper-asterisk">&#42;</span>}
      </div>
      {button && (
      <div
        onClick={button.onClick}
        role="presentation"
        className="input-wrapper-button"
      >
        <Translate id={button.id} defaultMessage={button.defaultMessage} />
      </div>
      )}
    </div>
    {children}
    <div className="input-wrapper-error-message">
      {errorMessage}
    </div>
  </div>
);

export default InputWrapper;

InputWrapper.propTypes = {
  // Input field which will be wrapped
  children: PropTypes.node.isRequired,
  // Message which will be shown on the tooltip above the field
  tooltip: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  // Indicator whether the red asterisk has to be shown
  required: PropTypes.bool,
  // Title displayed above the field
  title: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  // Button on the right side above the input
  button: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
  }),
  // Message displayed under the input
  errorMessage: PropTypes.string,
};

InputWrapper.defaultProps = {
  tooltip: null,
  required: false,
  title: null,
  button: null,
  errorMessage: null,
};
