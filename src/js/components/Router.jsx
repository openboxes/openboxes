import React from 'react';

import PropTypes from 'prop-types';
import Loadable from 'react-loadable';
import { connect } from 'react-redux';
import { BrowserRouter, Route, Switch } from 'react-router-dom';
import Alert from 'react-s-alert';
import { ClimbingBoxLoader } from 'react-spinners';

import MainLayoutRoute from 'components/Layout/MainLayoutRoute';
import Loading from 'components/Loading';

import 'react-s-alert/dist/s-alert-default.css';
import 'react-s-alert/dist/s-alert-css-effects/bouncyflip.css';

// TODO: Fix entering Inbound SM from list

const AsyncStockMovement = Loadable({
  loader: () => import('components/stock-movement-wizard/StockMovement'),
  loading: Loading,
});

const AsyncStockMovementInbound = Loadable({
  loader: () => import('components/stock-movement-wizard/StockMovementInbound'),
  loading: Loading,
});

const AsyncStockMovementCombinedShipments = Loadable({
  loader: () => import('components/stock-movement-wizard/StockMovementCombinedShipments'),
  loading: Loading,
});

const AsyncStockMovementRequest = Loadable({
  loader: () => import('components/stock-movement-wizard/StockMovementRequest'),
  loading: Loading,
});

const AsyncStockMovementVerifyRequest = Loadable({
  loader: () => import('components/stock-movement-wizard/StockMovementVerifyRequest'),
  loading: Loading,
});

const AsyncReceivingPage = Loadable({
  loader: () => import('components/receiving/ReceivingPage'),
  loading: Loading,
});

const AsyncPutAwayMainPage = Loadable({
  loader: () => import('components/put-away/PutAwayMainPage'),
  loading: Loading,
});

const AsyncManagement = Loadable({
  loader: () => import('components/stock-list-management/StocklistManagement'),
  loading: Loading,
});

const AsyncDashboard = Loadable({
  loader: () => import('components/dashboard/Dashboard'),
  loading: Loading,
});

const AsyncStockRequestDashboard = Loadable({
  loader: () => import('components/dashboard/StockRequestDashboard'),
  loading: Loading,
});

// TODO add megamenu and menu config
const AsyncInvoice = Loadable({
  loader: () => import('components/invoice/InvoiceWizard'),
  loading: Loading,
});

const AsyncStockTransfer = Loadable({
  loader: () => import('components/stock-transfer/StockTransferWizard'),
  loading: Loading,
});

const AsyncOutboundReturns = Loadable({
  loader: () => import('components/returns/outbound/OutboundReturnsWizard'),
  loading: Loading,
});

const AsyncInboundReturns = Loadable({
  loader: () => import('components/returns/inbound/InboundReturnsWizard'),
  loading: Loading,
});

const AsyncReplenishment = Loadable({
  loader: () => import('components/replenishment/ReplenishmentWizard'),
  loading: Loading,
});

const AsyncProductsConfiguration = Loadable({
  loader: () => import('components/products-configuration/ProductsConfigurationWizard'),
  loading: Loading,
});

const AsyncLocationsConfiguration = Loadable({
  loader: () => import('components/locations-configuration/LocationsConfigurationWizard'),
  loading: Loading,
});

const AsyncImportLocations = Loadable({
  loader: () => import('components/locations-configuration/ImportLocations'),
  loading: Loading,
});

const AsyncWelcomePage = Loadable({
  loader: () => import('components/locations-configuration/WelcomePage'),
  loading: Loading,
});

const AsyncLoadDataPage = Loadable({
  loader: () => import('components/load-demo-data/LoadDemoDataPage'),
  loading: Loading,
});

const AsyncResetInstancePage = Loadable({
  loader: () => import('components/reset-instance/ResettingInstanceInfoPage'),
  loading: Loading,
});

const Router = (props) => {
  const Dashboard = !props.supportedActivities.includes('MANAGE_INVENTORY') && props.supportedActivities.includes('SUBMIT_REQUEST')
    ? AsyncStockRequestDashboard
    : AsyncDashboard;

  return (
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
          <MainLayoutRoute path="/**/stockTransfer/createOutboundReturn/:outboundReturnId?" component={AsyncOutboundReturns} />
          <MainLayoutRoute path="/**/stockTransfer/createInboundReturn/:inboundReturnId?" component={AsyncInboundReturns} />
          <MainLayoutRoute path="/**/replenishment/create/:replenishmentId?" component={AsyncReplenishment} />
          <MainLayoutRoute path="/**/productsConfiguration/index" component={AsyncProductsConfiguration} />
          <MainLayoutRoute path="/**/locationsConfiguration/create/:locationId?" component={AsyncLocationsConfiguration} />
          <MainLayoutRoute path="/**/locationsConfiguration/upload" component={AsyncImportLocations} />
          <Route path="/**/locationsConfiguration/index" >
            <AsyncWelcomePage />
          </Route>
          <Route path="/**/loadData/index" ><AsyncLoadDataPage /></Route>
          <Route path="/**/resettingInstanceInfo/index">
            <AsyncResetInstancePage />
          </Route>
          <MainLayoutRoute path="/**/dashboard/:configId?" component={Dashboard} />
          <MainLayoutRoute path="/**/" component={Dashboard} />
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
};

const mapStateToProps = state => ({
  spinner: state.spinner.show,
  supportedActivities: state.session.supportedActivities,
});

export default connect(mapStateToProps, {})(Router);

Router.propTypes = {
  spinner: PropTypes.bool.isRequired,
  supportedActivities: PropTypes.arrayOf(PropTypes.string).isRequired,
};
