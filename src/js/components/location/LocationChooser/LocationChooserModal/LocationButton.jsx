import React, { useState } from 'react';

import PropTypes from 'prop-types';
import { RiMapPinLine } from 'react-icons/ri';

const LocationButton = ({ location, onClick }) => {
  const [isHovered, setIsHovered] = useState(false);

  const buttonStyle = () => {
    if (!location.backgroundColor || ['FFFFFF', 'FFFF'].includes(location.backgroundColor)) {
      return {};
    }
    const style = {
      borderColor: location.backgroundColor,
    };
    if (isHovered) {
      style.backgroundColor = location.backgroundColor;
    }
    return style;
  };

  const iconStyle = () => {
    if (!location.backgroundColor || ['FFFFFF', 'FFFF'].includes(location.backgroundColor)) {
      return {};
    }
    return {
      color: isHovered ? '#FFFFFF' : location.backgroundColor,
    };
  };

  return (
    <button
      onClick={() => onClick(location)}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      className="location-chooser__location-button"
      style={buttonStyle()}
    >
      <RiMapPinLine
        className="location-chooser__location-button__icon"
        style={iconStyle()}
      />
      <span className="location-chooser__location-button__title">
        {location.name}
      </span>
    </button>);
};

LocationButton.defaultProps = {
  onClick: undefined,
};

LocationButton.propTypes = {
  onClick: PropTypes.func,
  location: PropTypes.shape({
    name: PropTypes.string,
    backgroundColor: PropTypes.string,
  }).isRequired,
};

export default LocationButton;
