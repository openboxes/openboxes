import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import OpenboxesLogo from 'components/Layout/img/openboxes_logo.jpg';


const Logo = ({
  logoUrl, logoLabel, currentLocationName, history,
}) => (
  <div className="d-flex align-items-center gap-8 logo-wrapper">
    <div className="logo-square">
      {/* eslint-disable-next-line max-len */}
      {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events,jsx-a11y/no-noninteractive-element-interactions */}
      {logoUrl ? <img src={logoUrl} alt="Openboxes" width="40" height="40" onClick={() => history.push('/openboxes')} />
      : <img src={OpenboxesLogo} alt="Openboxes" />}
    </div>
    {/* Here add the location chooser */}
    <div className="d-flex align-items-center gap-8 location-name-wrapper">
      <span className="location-name-span">{currentLocationName && currentLocationName}</span>
      {logoLabel &&
        <div className="d-flex align-items-center justify-content-center location-label">
          <span>dev</span>
        </div>
        }
    </div>
  </div>
);


const mapStateToProps = state => ({
  logoUrl: state.session.logoUrl,
  logoLabel: state.session.logoLabel,
  currentLocationName: state.session.currentLocation.name,
});

export default withRouter(connect(mapStateToProps)(Logo));


Logo.propTypes = {
  logoUrl: PropTypes.string.isRequired,
  logoLabel: PropTypes.string.isRequired,
  currentLocationName: PropTypes.string.isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
};
