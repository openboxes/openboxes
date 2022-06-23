import React from 'react';

import PropTypes from 'prop-types';
import { SliderPicker } from 'react-color';
import { Tooltip } from 'react-tippy';

import Input from 'utils/Input';

const ColorPicker = ({ value, onChange }) => {
  const handleChange = (color) => {
    const hex = color.hex.split('#')[1];
    onChange(hex);
  };

  const colorHex = value ? `#${value}` : '#ffffff';

  return (
    <Tooltip
      html={
        <div style={{ width: 350 }}>
          <SliderPicker
            color={colorHex}
            onChangeComplete={handleChange}
          />
        </div>
      }
      theme="transparent"
      arrow="true"
      trigger="click"
      interactive
      delay="150"
      duration="250"
    >
      <div className="input-group">
        <div className="input-group-prepend">
          <span className="input-group-text" style={{ backgroundColor: colorHex }}>&nbsp;&nbsp;</span>
        </div>
        <Input value={value} onChange={val => onChange(val)} />
        <div className="input-group-append">
          <button type="button" className="input-group-text" onClick={() => onChange(null)}>
            <i className="fa fa-close" />
          </button>
        </div>
      </div>
    </Tooltip>
  );
};

export default ColorPicker;

ColorPicker.propTypes = {
  onChange: PropTypes.func.isRequired,
  value: PropTypes.string,
};

ColorPicker.defaultProps = {
  value: null,
};
