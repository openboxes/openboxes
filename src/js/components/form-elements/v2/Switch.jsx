import React, { useEffect, useState } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

import useTranslate from 'hooks/useTranslate';
import Translate from 'utils/Translate';
import CustomTooltip from 'wrappers/CustomTooltip';

import './style.scss';

const Switch = ({
  defaultValue,
  titles,
  onChange,
  className,
  ...fieldProps
}) => {
  const translate = useTranslate();
  const [value, changeValue] = useState(fieldProps?.value ?? defaultValue);
  const toggleId = _.uniqueId();

  const onChangeValue = () => {
    changeValue((previousValue) => {
      onChange?.(!previousValue);
      return !previousValue;
    });
  };

  useEffect(() => {
    if (_.isBoolean(fieldProps?.value)) {
      changeValue(fieldProps?.value);
    }
  }, [fieldProps?.value]);

  const [tooltipId, tooltipDefault] = value
    ? [titles?.checked?.tooltipLabel, titles?.checked?.defaultTooltipLabel]
    : [titles?.unchecked?.tooltipLabel, titles?.unchecked?.defaultTooltipLabel];
  const showTooltip = Boolean(tooltipId || tooltipDefault);
  const tooltipText = showTooltip ? translate(tooltipId, tooltipDefault) : '';

  return (
    <CustomTooltip content={tooltipText} show={showTooltip}>
      <div className={`switch-container ${className}`}>
        <label htmlFor={`toggle-${toggleId}`} className="switch">
          <input
            id={`toggle-${toggleId}`}
            type="checkbox"
            defaultChecked={value}
            onChange={onChangeValue}
            checked={value}
            {...fieldProps}
          />
          <div className="slider" />
        </label>
        <div className="switch-title">
          {value
            ? (
              <Translate
                id={titles?.checked?.id}
                defaultMessage={titles?.checked?.defaultMessage}
              />
            ) : (
              <Translate
                id={titles?.unchecked?.id}
                defaultMessage={titles?.unchecked?.defaultMessage}
              />
            )}
        </div>
      </div>
    </CustomTooltip>
  );
};

export default Switch;

Switch.propTypes = {
  defaultValue: PropTypes.bool,
  // Displayed titles according to current state
  // of the switch (checked / unchecked)
  titles: PropTypes.shape({
    checked: PropTypes.shape({
      id: PropTypes.string,
      defaultMessage: PropTypes.string,
      tooltipLabel: PropTypes.string,
      defaultTooltipLabel: PropTypes.string,
    }),
    unchecked: PropTypes.shape({
      id: PropTypes.string,
      defaultMessage: PropTypes.string,
      tooltipLabel: PropTypes.string,
      defaultTooltipLabel: PropTypes.string,
    }),
  }),
  onChange: PropTypes.func,
  className: PropTypes.string,
};

Switch.defaultProps = {
  defaultValue: false,
  titles: {
    checked: {
      id: '',
      defaultMessage: '',
    },
    unchecked: {
      id: '',
      defaultMessage: '',
    },
  },
  onChange: () => {},
  className: '',
};
