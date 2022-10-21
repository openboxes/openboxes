import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import OpenboxesLogo from 'components/Layout/img/openboxes_logo.jpg';


const Logo = ({
  logoUrl, history,
}) => (
  <div className="d-flex align-items-center logo-wrapper">
    <div className="logo-square">
      {logoUrl
          ? <img
            src={logoUrl}
            alt="Openboxes"
            width="40"
            height="40"
            role="presentation"
            onClick={() => history.push('/openboxes')}
          />
          : <img src={OpenboxesLogo} alt="Openboxes" />}
    </div>
  </div>
);


const mapStateToProps = state => ({
  logoUrl: state.session.logoUrl,
});

export default withRouter(connect(mapStateToProps)(Logo));


Logo.propTypes = {
  logoUrl: PropTypes.string,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
};

Logo.defaultProps = {
  logoUrl: null,
};
