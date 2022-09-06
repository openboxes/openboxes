import React from 'react';

import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

import Translate from 'utils/Translate';

const LocationChooserButton = ({ onToggle, location, envTag }) => {
  const buttonStyle = () => {
    if (!location.backgroundColor || ['FFFFFF', 'FFFF'].includes(location.backgroundColor)) {
      return {
        borderColor: '#D6D8DC',
      };
    }
    return {
      outlineColor: location.backgroundColor,
      borderColor: location.backgroundColor,
    };
  };
  return (
    <button
      type="button"
      className="location-chooser__button"
      style={buttonStyle()}
      onClick={onToggle}
    >
      {
        location && location.name && location.name.length > 20
          ? (
            <Tooltip
              title={location.name}
              delay="500"
              duration="250"
              hideDelay="50"
              className="location-chooser__button-title"
            >
              { location.name }
            </Tooltip>
          ) : (
            <span className="location-chooser__button-title">
              { location.name ||
              <Translate
                id="react.dashboard.chooseLocation.label"
                defaultMessage="Choose Location"
              /> }
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
