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

// TODO: Fix entering Inbound SM from list

const AsyncStockMovement = Loadable({
  loader: () => import('./stock-movement-wizard/StockMovement'),
  loading: Loading,
});

const AsyncStockMovementInbound = Loadable({
  loader: () => import('./stock-movement-wizard/StockMovementInbound'),
  loading: Loading,
});

const AsyncStockMovementCombinedShipments = Loadable({
  loader: () => import('./stock-movement-wizard/StockMovementCombinedShipments'),
  loading: Loading,
});

const AsyncStockMovementRequest = Loadable({
  loader: () => import('./stock-movement-wizard/StockMovementRequest'),
  loading: Loading,
});

const AsyncStockMovementVerifyRequest = Loadable({
  loader: () => import('./stock-movement-wizard/StockMovementVerifyRequest'),
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

const AsyncTablero = Loadable({
  loader: () => import('./tablero/Tablero'),
  loading: Loading,
});

// TODO add megamenu and menu config
const AsyncInvoice = Loadable({
  loader: () => import('./invoice/InvoiceWizard'),
  loading: Loading,
});

const AsyncStockTransfer = Loadable({
  loader: () => import('./stock-transfer/StockTransferWizard'),
  loading: Loading,
});

const Router = props => (
  <div>
    <BrowserRouter>
      <Switch>
        <MainLayoutRoute path="/**/putAway/create/:putAwayId?" component={AsyncPutAwayMainPage} />
        <MainLayoutRoute path="/**/stockMovement/createOutbound/:stockMovementId?" component={AsyncStockMovement} />
        <MainLayoutRoute path="/**/stockMovement/createInbound/:stockMovementId?" component={AsyncStockMovementInbound} />
        <MainLayoutRoute path="/**/stockMovement/createCombinedShipments/:stockMovementId?" component={AsyncStockMovementCombinedShipments} />
        <MainLayoutRoute path="/**/stockMovement/createRequest/:stockMovementId?" component={AsyncStockMovementRequest} />
        <MainLayoutRoute path="/**/stockMovement/verifyRequest/:stockMovementId?" component={AsyncStockMovementVerifyRequest} />
        <MainLayoutRoute path="/**/stockMovement/create/:stockMovementId?" component={AsyncStockMovement} />
        <MainLayoutRoute path="/**/partialReceiving/create/:shipmentId" component={AsyncReceivingPage} />
        <MainLayoutRoute path="/**/stocklistManagement/index/:productId?" component={AsyncManagement} />
        <MainLayoutRoute path="/**/invoice/create/:invoiceId?" component={AsyncInvoice} />
        <MainLayoutRoute path="/**/stockTransfer/create/:stockTransferId?" component={AsyncStockTransfer} />
        <MainLayoutRoute path="/**/" component={AsyncTablero} />
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
