import React, { useId } from 'react';

import PropTypes from 'prop-types';
import { SliderPicker } from 'react-color';

import Input from 'utils/Input';
import PortalTooltip from 'wrappers/PortalTooltip';

const ColorPicker = ({ value, onChange }) => {
  const tooltipId = useId();

  const handleChange = (color) => {
    const hex = color.hex.split('#')[1];
    onChange(hex);
  };

  const colorHex = value ? `#${value}` : '#ffffff';

  return (
    <>
      <div className="input-group" data-tooltip-id={tooltipId}>
        <div className="input-group-prepend">
          <span className="input-group-text" style={{ backgroundColor: colorHex }}>&nbsp;&nbsp;</span>
        </div>
        <Input value={value} onChange={(val) => onChange(val)} />
        <div className="input-group-append">
          <button type="button" className="input-group-text" onClick={() => onChange(null)}>
            <i className="fa fa-close" />
          </button>
        </div>
      </div>
      <PortalTooltip id={tooltipId} openOnClick clickable>
        <div style={{ width: 350 }}>
          <SliderPicker
            color={colorHex}
            onChangeComplete={handleChange}
          />
        </div>
      </PortalTooltip>
    </>
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
