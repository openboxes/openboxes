import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Link, withRouter } from 'react-router-dom';

import { DASHBOARD_URL } from 'consts/applicationUrls';

const Logo = ({
  logoUrl,
}) => (
  <div className="d-flex align-items-center logo-wrapper" data-testid="logo-wrapper">
    <div className="logo-square" aria-label="logo">
      <Link to={DASHBOARD_URL.base}>
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

const mapStateToProps = (state) => ({
  logoUrl: state.session.logoUrl,
});

export default withRouter(connect(mapStateToProps)(Logo));

Logo.propTypes = {
  logoUrl: PropTypes.string,
};

Logo.defaultProps = {
  logoUrl: '',
};
