import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Link, withRouter } from 'react-router-dom';


const Logo = ({
  logoUrl,
  defaultLogoUrl,
}) => {
  const [imgSrc, setImgSrc] = useState('');
  const [defaultLogo, setDefaultLogo] = useState('');
  useEffect(() => {
    if (logoUrl) {
      setImgSrc(logoUrl);
    }
  }, [logoUrl]);

  useEffect(() => {
    if (defaultLogoUrl) {
      setDefaultLogo(defaultLogoUrl);
    }
  }, [defaultLogoUrl]);

  const handleError = () => {
    if (defaultLogo) {
      setImgSrc(defaultLogo);
    }
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
  defaultLogoUrl: state.session.defaultLogoUrl,
});

export default withRouter(connect(mapStateToProps)(Logo));


Logo.propTypes = {
  logoUrl: PropTypes.string,
  defaultLogoUrl: PropTypes.string,
};

Logo.defaultProps = {
  logoUrl: '',
  defaultLogoUrl: '',
};
