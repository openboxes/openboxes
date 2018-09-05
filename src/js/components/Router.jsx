import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { BrowserRouter, Switch } from 'react-router-dom';
import { ClimbingBoxLoader } from 'react-spinners';
import Alert from 'react-s-alert';

import 'react-s-alert/dist/s-alert-default.css';
import 'react-s-alert/dist/s-alert-css-effects/bouncyflip.css';

import StockMovement from './stock-movement-wizard/StockMovement';
import ReceivingPage from './receiving/ReceivingPage';
import MainLayoutRoute from './Layout/MainLayoutRoute';
import PutAwayMainPage from './put-away/PutAwayMainPage';

const Router = props => (
  <div>
    <BrowserRouter>
      <Switch>
        <MainLayoutRoute path="/**/putAway" component={PutAwayMainPage} />
        <MainLayoutRoute path="/**/stockMovement/index/:stockMovementId?" component={StockMovement} />
        <MainLayoutRoute path="/**/partialReceiving/create/:shipmentId" component={ReceivingPage} />
      </Switch>
    </BrowserRouter>
    <div className="spinner-container">
      <ClimbingBoxLoader
        color="#0c769e"
        loading={props.spinner}
        style={{ top: '40%', lefft: '50%' }}
      />
    </div>
    <Alert
      timeout={5000}
      stack={{ limit: 3 }}
      offset={20}
      html
      position="top-right"
      effect="bouncyflip"
    />
  </div>
);

const mapStateToProps = state => ({
  spinner: state.spinner.show,
});

export default connect(mapStateToProps, {})(Router);

Router.propTypes = {
  spinner: PropTypes.bool.isRequired,
};
