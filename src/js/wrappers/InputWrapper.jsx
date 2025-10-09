import React from 'react';

import PropTypes from 'prop-types';
import { RiQuestionLine } from 'react-icons/ri';
import { Tooltip } from 'react-tippy';

import Translate from 'utils/Translate';
import CustomTooltip from 'wrappers/CustomTooltip';

import './style.scss';

const InputWrapper = ({
  children,
  tooltip,
  required,
  button,
  title,
  errorMessage,
  className,
  inputId,
  labelPosition,
  hideErrorMessageWrapper,
  customTooltip,
  value,
}) => (
  <div className={`input-wrapper-container ${className} input-wrapper-label-position-${labelPosition}`}>
    <div className="input-wrapper-title">
      <div className="input-wrapper-label">
        <label htmlFor={inputId} className="m-0">
          {title && <Translate id={title?.id} defaultMessage={title?.defaultMessage} />}
        </label>
        {tooltip && (
        <Tooltip
          html={(
            <span className="p-1">
              <Translate id={tooltip.id} defaultMessage={tooltip.defaultMessage} />
            </span>
              )}
        >
          <span className="input-wrapper-tooltip">
            <RiQuestionLine className="ml-1" />
          </span>
        </Tooltip>
        )}
        {required && <span className="input-wrapper-asterisk ml-1">&#42;</span>}
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
    <CustomTooltip
      show={customTooltip}
      content={value}
    >
      {children}
    </CustomTooltip>
    {!hideErrorMessageWrapper && (
      <div className="input-wrapper-error-message">
        {errorMessage}
      </div>
    )}
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
  className: PropTypes.string,
  // id of an input element to be mapped to label tag
  inputId: PropTypes.string,
  // input label position
  labelPosition: PropTypes.oneOf(['top', 'bottom', 'left', 'right']),
  hideErrorMessageWrapper: PropTypes.bool,
  customTooltip: PropTypes.bool,
  value: PropTypes.string,
};

InputWrapper.defaultProps = {
  tooltip: null,
  required: false,
  title: null,
  button: null,
  errorMessage: null,
  className: '',
  inputId: undefined,
  labelPosition: 'top',
  hideErrorMessageWrapper: false,
  customTooltip: false,
  value: '',
};
