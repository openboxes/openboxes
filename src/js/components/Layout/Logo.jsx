import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import OpenboxesLogo from 'components/Layout/img/openboxes_logo.jpg';
import LocationChooser from 'components/location/LocationChooser';


const Logo = ({
  logoUrl, history,
}) => (
  <div className="d-flex align-items-center gap-8 logo-wrapper">
    <div className="logo-square">
      {logoUrl ? <img src={logoUrl} alt="Openboxes" width="40" height="40" onClick={() => history.push('/openboxes')} role="presentation" />
      : <img src={OpenboxesLogo} alt="Openboxes" />}
    </div>
    <LocationChooser />
  </div>
);


const mapStateToProps = state => ({
  logoUrl: state.session.logoUrl,
});

export default withRouter(connect(mapStateToProps)(Logo));


Logo.propTypes = {
  logoUrl: PropTypes.string.isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
};
