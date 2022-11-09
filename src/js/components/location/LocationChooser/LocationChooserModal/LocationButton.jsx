import React from 'react';

import PropTypes from 'prop-types';
import { RiMapPinLine } from 'react-icons/ri';

const LocationButton = ({ location, onClick }) => {
  const buttonStyle = () => {
    if (!location.backgroundColor) return { '--location-color': 'unset' };

    const normalizeColor = location.backgroundColor.replace('#', '').toUpperCase();
    if (['FFFFFF', 'FFFF'].includes(normalizeColor)) return { '--location-color': 'unset' };

    return { '--location-color': `#${normalizeColor}` };
  };

  return (
    <button
      onClick={() => onClick(location)}
      className="location-chooser__location-button"
      style={buttonStyle()}
    >
      <RiMapPinLine className="location-chooser__location-button__icon" />
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
