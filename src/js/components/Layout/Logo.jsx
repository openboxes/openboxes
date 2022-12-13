import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Link, withRouter } from 'react-router-dom';


const Logo = ({
  logoUrl,
}) => (
  <div className="d-flex align-items-center logo-wrapper" data-testid="logo-wrapper">
    <div className="logo-square">
      <Link to="/openboxes">
        <img
          src={logoUrl}
          alt="Openboxes"
          width="40"
          height="40"
        />
      </Link>
    </div>
  </div>
);


const mapStateToProps = state => ({
  logoUrl: state.session.logoUrl,
});

export default withRouter(connect(mapStateToProps)(Logo));


Logo.propTypes = {
  logoUrl: PropTypes.string,
};

Logo.defaultProps = {
  logoUrl: '',
};
