import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import Alert from 'react-s-alert';
import { ClimbingBoxLoader } from 'react-spinners';

import CustomAlert from 'components/dashboard/CustomAlert';
import SwitchWrapper from 'components/SwitchWrapper';
import useConnectionListener from 'hooks/useConnectionListener';

import 'react-s-alert/dist/s-alert-default.css';
import 'react-s-alert/dist/s-alert-css-effects/bouncyflip.css';

const Router = (props) => {
  useConnectionListener();

  return (
    <div>
      <BrowserRouter>
        <SwitchWrapper />
      </BrowserRouter>
      <div className="spinner-container">
        <ClimbingBoxLoader
          color="#0c769e"
          loading={props.spinner}
          style={{ top: '40%', lefft: '50%' }}
        />
      </div>
      <Alert
        timeout={props.notificationAutohideDelay}
        stack={{ limit: 3 }}
        contentTemplate={CustomAlert}
        position="top-right"
        effect="bouncyflip"
        offset={20}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  spinner: state.spinner.show,
  notificationAutohideDelay: state.session.notificationAutohideDelay,
});

export default connect(mapStateToProps, {})(Router);

Router.propTypes = {
  spinner: PropTypes.bool.isRequired,
  notificationAutohideDelay: PropTypes.number.isRequired,
};
