import React, { useState } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

import './style.scss';

const Switch = ({
  defaultValue,
  titles,
  onChange,
  ...fieldProps
}) => {
  const [value, changeValue] = useState(defaultValue);
  const toggleId = _.uniqueId();

  const onChangeValue = () => {
    changeValue((previousValue) => {
      onChange?.(!previousValue);
      return !previousValue;
    });
  };

  return (
    <div className="switch-container">
      <label htmlFor={`toggle-${toggleId}`} className="switch">
        <input
          id={`toggle-${toggleId}`}
          type="checkbox"
          defaultChecked={value}
          onChange={onChangeValue}
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
    }),
    unchecked: PropTypes.shape({
      id: PropTypes.string,
      defaultMessage: PropTypes.string,
    }),
  }),
  onChange: PropTypes.func,
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
};
