import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { BrowserRouter, Switch } from 'react-router-dom';
import { ClimbingBoxLoader } from 'react-spinners';
import Alert from 'react-s-alert';
import Loadable from 'react-loadable';

import 'react-s-alert/dist/s-alert-default.css';
import 'react-s-alert/dist/s-alert-css-effects/bouncyflip.css';

import MainLayoutRoute from './Layout/MainLayoutRoute';
import Loading from './Loading';

const AsyncStockMovement = Loadable({
  loader: () => import('./stock-movement-wizard/StockMovement'),
  loading: Loading,
});

const AsyncReceivingPage = Loadable({
  loader: () => import('./receiving/ReceivingPage'),
  loading: Loading,
});

const AsyncPutAwayMainPage = Loadable({
  loader: () => import('./put-away/PutAwayMainPage'),
  loading: Loading,
});

const AsyncManagement = Loadable({
  loader: () => import('./stock-list-management/StocklistManagement'),
  loading: Loading,
});

const Router = props => (
  <div>
    <BrowserRouter>
      <Switch>
        <MainLayoutRoute path="/putAway/create/:putAwayId?" component={AsyncPutAwayMainPage} />
        <MainLayoutRoute path="/stockMovement/create/:stockMovementId?" component={AsyncStockMovement} />
        <MainLayoutRoute path="/partialReceiving/create/:shipmentId" component={AsyncReceivingPage} />
        <MainLayoutRoute path="/stocklistManagement/index/:productId?" component={AsyncManagement} />
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
      timeout="none"
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
