import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Link, withRouter } from 'react-router-dom';

import OpenboxesLogo from 'components/Layout/img/openboxes_logo.jpg';


const Logo = ({
  logoUrl,
}) => {
  const [imgSrc, setImgSrc] = useState(null);
  useEffect(() => {
    setImgSrc(logoUrl || OpenboxesLogo);
  }, [logoUrl]);

  const handleError = () => {
    setImgSrc(OpenboxesLogo);
  };
  return (
    <div className="d-flex align-items-center logo-wrapper">
      <div className="logo-square">
        <Link to="/openboxes">
          <img
            src={imgSrc}
            onError={handleError}
            alt="Openboxes"
            width="40"
            height="40"
          />
        </Link>
      </div>
    </div>
  );
};


const mapStateToProps = state => ({
  logoUrl: state.session.logoUrl,
});

export default withRouter(connect(mapStateToProps)(Logo));


Logo.propTypes = {
  logoUrl: PropTypes.string,
};

Logo.defaultProps = {
  logoUrl: null,
};
