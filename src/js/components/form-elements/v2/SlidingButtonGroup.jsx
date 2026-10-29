import React, { useEffect, useRef, useState } from 'react';

import PropTypes from 'prop-types';

import useTranslate from 'hooks/useTranslate';
import Translate from 'utils/Translate';
import CustomTooltip from 'wrappers/CustomTooltip';

import 'components/form-elements/v2/style.scss';

const SlidingButtonGroup = ({
  options, defaultOption, onChange, className, style,
}) => {
  const translate = useTranslate();
  const [activeOption, setActiveOption] = useState(defaultOption);
  const sliderPositionRef = useRef(null);
  const buttonRefs = useRef([]);

  const updateSliderPosition = () => {
    const activeButton = buttonRefs.current[options.findIndex((o) => o.value === activeOption)];
    if (!activeButton) {
      return;
    }
    sliderPositionRef.current.style.width = `${activeButton.offsetWidth}px`;
    sliderPositionRef.current.style.transform = `translateX(${activeButton.offsetLeft}px)`;
  };

  useEffect(() => {
    updateSliderPosition();
  }, [activeOption, options]);

  const handleClick = (option) => {
    if (option.disabled) {
      return;
    }
    setActiveOption(option.value);
    onChange?.(option.value);
  };

  return (
    <div className={`sliding-button-group ${className ? `${className}` : ''}`} style={style} role="group">
      <div className="sliding-button-group__slider" ref={sliderPositionRef} />
      {options.map((option, index) => {
        // We allow displaying a tooltip even if the button group is disabled so we need to
        // determine which tooltip we should be displaying (if any).
        const [tooltipLabel, tooltipDefault] = option.disabled
          ? [option.disabledTooltipLabel, option.defaultDisabledTooltipLabel]
          : [option.tooltipLabel, option.defaultTooltipLabel];

        const showTooltip = Boolean(tooltipLabel || tooltipDefault);
        const tooltipText = showTooltip ? translate(tooltipLabel, tooltipDefault) : '';

        return (
          <CustomTooltip content={tooltipText} show={showTooltip}>
            <button
              key={option.value}
              ref={(el) => { buttonRefs.current[index] = el; }}
              type="button"
              role="radio"
              aria-checked={activeOption === option.value}
              aria-disabled={option.disabled}
              className="sliding-button-group__button"
              onClick={() => handleClick(option)}
            >
              <Translate id={option.label} defaultMessage={option.defaultLabel} />
            </button>
          </CustomTooltip>
        );
      })}
    </div>
  );
};

export default SlidingButtonGroup;

SlidingButtonGroup.propTypes = {
  options: PropTypes.arrayOf(PropTypes.shape({
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
    tooltipLabel: PropTypes.string,
    defaultTooltipLabel: PropTypes.string,
    disabled: PropTypes.bool,
    disabledTooltipLabel: PropTypes.string,
    defaultDisabledTooltipLabel: PropTypes.string,
  })).isRequired,
  defaultOption: PropTypes.string.isRequired,
  onChange: PropTypes.func,
  className: PropTypes.string,
  style: PropTypes.shape({}),
};

SlidingButtonGroup.defaultProps = {
  onChange: () => {},
  className: '',
  style: {},
};
