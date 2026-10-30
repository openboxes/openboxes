import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';
import CustomTooltip from 'wrappers/CustomTooltip';

import 'components/location/LocationChooser/LocationChooserButton/LocationChooserButton.scss';

const LocationChooserButton = ({ onToggle, location, envTag }) => {
  const buttonStyle = () => {
    if (!location.backgroundColor) return { '--location-color': 'unset' };

    const normalizeColor = location.backgroundColor.replace('#', '').toUpperCase();
    if (['FFFFFF', 'FFFF'].includes(normalizeColor)) return { '--location-color': 'unset' };

    return { '--location-color': `#${normalizeColor}` };
  };
  return (
    <button
      type="button"
      className="location-chooser__button"
      aria-label="location-chooser"
      style={buttonStyle()}
      onClick={onToggle}
    >
      {
        location && location.name && location.name.length > 20
          ? (
            <CustomTooltip
              content={<div className="custom-tooltip">{location.name}</div>}
              delayShow={500}
              className="location-chooser__button-title"
            >
              { location.name }
            </CustomTooltip>
          ) : (
            <span className="location-chooser__button-title">
              { location.name
              || (
              <Translate
                id="react.dashboard.chooseLocation.label"
                defaultMessage="Choose Location"
              />
              ) }
            </span>
          )
      }
      { envTag && <strong className="location-chooser__button-tag">{envTag}</strong> }
    </button>
  );
};

LocationChooserButton.defaultProps = {
  location: {
    name: 'Choose Location',
    backgroundColor: undefined,
  },
  envTag: undefined,
};

LocationChooserButton.propTypes = {
  onToggle: PropTypes.func.isRequired,
  location: PropTypes.shape({
    name: PropTypes.string,
    backgroundColor: PropTypes.string,
  }),
  envTag: PropTypes.string,
};

export default LocationChooserButton;
