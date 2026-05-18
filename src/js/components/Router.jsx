import React, { lazy, Suspense, useMemo } from 'react';

import queryString from 'query-string';
import { useSelector } from 'react-redux';
import {
  BrowserRouter, Navigate, Route, Routes,
} from 'react-router-dom';
import { ClimbingBoxLoader } from 'react-spinners';
import { ToastContainer } from 'react-toastify';
import {
  getCurrentLocationSupportedActivities,
  getNotificationAutohideDelay,
  getSpinner,
} from 'selectors';

import MainLayout from 'components/Layout/v2/MainLayout';
import ActivityCode from 'consts/activityCode';
import { DASHBOARD_URL } from 'consts/applicationUrls';
import useConnectionListener from 'hooks/useConnectionListener';
import FlashScopeListenerWrapper from 'wrappers/FlashScopeListenerWrapper';

import 'react-toastify/dist/ReactToastify.css';
// TODO: Fix entering Inbound SM from list

const AsyncStockMovement = lazy(() => import('components/stock-movement-wizard/StockMovement'));

const AsyncStockMovementInbound = lazy(() => import('components/stock-movement-wizard/inboundV2/Inbound'));

const AsyncStockMovementCombinedShipments = lazy(() => import('components/stock-movement-wizard/StockMovementCombinedShipments'));

const AsyncStockMovementRequest = lazy(() => import('components/stock-movement-wizard/StockMovementRequest'));

const AsyncStockMovementVerifyRequest = lazy(() => import('components/stock-movement-wizard/StockMovementVerifyRequest'));

const AsyncReceivingPage = lazy(() => import('components/receiving/ReceivingPage'));

const AsyncPutAwayMainPage = lazy(() => import('components/put-away/PutAwayMainPage'));

const AsyncManagement = lazy(() => import('components/stock-list-management/StocklistManagement'));

const AsyncDashboard = lazy(() => import('components/dashboard/Dashboard'));

const AsyncStockRequestDashboard = lazy(() => import('components/dashboard/StockRequestDashboard'));

// TODO add megamenu and menu config
const AsyncInvoice = lazy(() => import('components/invoice/create/InvoiceWizard'));

const AsyncInvoiceList = lazy(() => import('components/invoice/list/InvoiceList'));

const AsyncStockTransfer = lazy(() => import('components/stock-transfer/StockTransferWizard'));

const AsyncOutboundReturns = lazy(() => import('components/returns/outbound/OutboundReturnsWizard'));

const AsyncInboundReturns = lazy(() => import('components/returns/inbound/InboundReturnsWizard'));

const AsyncReplenishment = lazy(() => import('components/replenishment/ReplenishmentWizard'));

const AsyncProductsConfiguration = lazy(() => import('components/products-configuration/ProductsConfigurationWizard'));

const AsyncLocationsConfiguration = lazy(() => import('components/locations-configuration/LocationsConfigurationWizard'));

const AsyncImportLocations = lazy(() => import('components/locations-configuration/ImportLocations'));

const AsyncWelcomePage = lazy(() => import('components/locations-configuration/WelcomePage'));

const AsyncLoadDataPage = lazy(() => import('components/load-demo-data/LoadDemoDataPage'));

const AsyncResetInstancePage = lazy(() => import('components/reset-instance/ResettingInstanceInfoPage'));

const AsyncPurchaseOrderList = lazy(() => import('components/purchaseOrder/PurchaseOrderList'));

const AsyncStockList = lazy(() => import('components/stock-list/StockList'));

const AsyncProductsList = lazy(() => import('components/products/ProductsList'));

const AsyncStockMovementInboundList = lazy(() => import('components/stock-movement/inbound/StockMovementInboundList'));

const AsyncStockMovementOutboundList = lazy(() => import('components/stock-movement/outbound/StockMovementOutboundList'));

const AsyncProductSupplierList = lazy(() => import('components/productSupplier/ProductSupplierList'));

const AsyncProductSupplierCreatePage = lazy(() => import('components/productSupplier/create/ProductSupplierForm'));

const AsyncOutboundImport = lazy(() => import('components/stock-movement-wizard/outboundImport/OutboundImport'));

const AsyncCycleCount = lazy(() => import('components/cycleCount/CycleCount'));

const AsyncCycleCountCountStep = lazy(() => import('components/cycleCount/toCountTab/CountStep'));

const AsyncCycleCountResolveStep = lazy(() => import('components/cycleCount/toResolveTab/ResolveStep'));

const AsyncCycleCountReporting = lazy(() => import('components/cycleCountReporting/CycleCountReporting'));

const AsyncReorderReport = lazy(() => import('components/reporting/reorderReport/ReorderReport'));

const AsyncExpirationHistoryReport = lazy(() => import('components/reporting/expirationHistoryReport/ExpirationHistoryReport'));

const StockMovementList = (props) => {
  const parsedSearchQuery = queryString.parse(props?.location?.search);
  const direction = parsedSearchQuery?.direction?.toUpperCase();
  switch (direction) {
    case 'INBOUND':
      return <AsyncStockMovementInboundList {...props} />;
    case 'OUTBOUND': {
      return (
        <AsyncStockMovementOutboundList
          {...props}
          sourceType={parsedSearchQuery?.sourceType?.toUpperCase()}
        />
      );
    }
    default:
      return <Navigate to={DASHBOARD_URL.base} />;
  }
};

const AsyncStockTransferList = lazy(() => import('components/stock-transfer/list/StockTransferList'));

const Router = () => {
  useConnectionListener();

  const spinner = useSelector(getSpinner);
  const supportedActivities = useSelector(getCurrentLocationSupportedActivities);
  const notificationAutohideDelay = useSelector(getNotificationAutohideDelay);

  const Dashboard = useMemo(
    () => (!supportedActivities?.includes(ActivityCode.MANAGE_INVENTORY)
    && supportedActivities?.includes(ActivityCode.SUBMIT_REQUEST)
      ? AsyncStockRequestDashboard
      : AsyncDashboard), [supportedActivities],
  );

  return (
    <div>
      <BrowserRouter basename={window.CONTEXT_PATH || '/openboxes'}>
        <FlashScopeListenerWrapper>
          <Suspense fallback={<div style={{ textAlign: 'center', padding: '50px' }}>Loading...</div>}>
            <Routes>
              <Route element={<MainLayout />}>
                <Route path="/purchaseOrder/list" element={<AsyncPurchaseOrderList />} />
                <Route path="/requisitionTemplate/list" element={<AsyncStockList />} />
                <Route path="/product/list" element={<AsyncProductsList />} />
                <Route path="/stockTransfer/list" element={<AsyncStockTransferList />} />
                <Route path="/productSupplier/list" element={<AsyncProductSupplierList />} />
                <Route path="/productSupplier/create/:productSupplierId?" element={<AsyncProductSupplierCreatePage />} />
                <Route path="/dashboard/:configId?" element={<Dashboard />} />
                <Route path="/" element={<Dashboard />} />
                <Route path="/putAway/create/:putAwayId?" element={<AsyncPutAwayMainPage />} />
                <Route path="/stockMovement/list" element={<StockMovementList />} />
                <Route path="/stockMovement/createOutbound/:stockMovementId?" element={<AsyncStockMovement />} />
                <Route path="/stockMovement/importOutboundStockMovement" element={<AsyncOutboundImport />} />
                <Route path="/report/expirationHistoryReport" element={<AsyncExpirationHistoryReport />} />
                <Route path="/inventory/reorderReport" element={<AsyncReorderReport />} />
                <Route path="/inventory/cycleCount/count" element={<AsyncCycleCountCountStep />} />
                <Route path="/inventory/cycleCount/resolve" element={<AsyncCycleCountResolveStep />} />
                <Route path="/inventory/cycleCount/reporting" element={<AsyncCycleCountReporting />} />
                <Route path="/inventory/cycleCount" element={<AsyncCycleCount />} />
                <Route path="/stockMovement/createInbound/:stockMovementId?" element={<AsyncStockMovementInbound />} />
                <Route path="/stockMovement/createCombinedShipments/:stockMovementId?" element={<AsyncStockMovementCombinedShipments />} />
                <Route path="/stockMovement/createRequest/:stockMovementId?" element={<AsyncStockMovementRequest />} />
                <Route path="/stockMovement/verifyRequest/:stockMovementId?" element={<AsyncStockMovementVerifyRequest />} />
                <Route path="/stockMovement/create/:stockMovementId?" element={<AsyncStockMovement />} />
                <Route path="/partialReceiving/create/:shipmentId" element={<AsyncReceivingPage />} />
                <Route path="/stocklistManagement/index/:productId?" element={<AsyncManagement />} />
                <Route path="/invoice/create/:invoiceId?" element={<AsyncInvoice />} />
                <Route path="/invoice/list" element={<AsyncInvoiceList />} />
                <Route path="/stockTransfer/create/:stockTransferId?" element={<AsyncStockTransfer />} />
                <Route path="/stockTransfer/createOutboundReturn/:outboundReturnId?" element={<AsyncOutboundReturns />} />
                <Route path="/stockTransfer/createInboundReturn/:inboundReturnId?" element={<AsyncInboundReturns />} />
                <Route path="/replenishment/create/:replenishmentId?" element={<AsyncReplenishment />} />
                <Route path="/productsConfiguration/index" element={<AsyncProductsConfiguration />} />
                <Route path="/locationsConfiguration/create/:locationId?" element={<AsyncLocationsConfiguration />} />
                <Route path="/locationsConfiguration/upload" element={<AsyncImportLocations />} />
              </Route>
              <Route path="/locationsConfiguration/index" element={<AsyncWelcomePage />} />
              <Route path="/loadData/index" element={<AsyncLoadDataPage />} />
              <Route path="/resettingInstanceInfo/index" element={<AsyncResetInstancePage />} />
            </Routes>
          </Suspense>
        </FlashScopeListenerWrapper>
      </BrowserRouter>
      <div className="spinner-container">
        <ClimbingBoxLoader
          color="#0c769e"
          loading={spinner}
          style={{ top: '40%', left: '50%' }}
        />
      </div>
      {/* <ToastContainer */}
      {/*   autoClose={notificationAutohideDelay} */}
      {/*   limit={3} */}
      {/*   position="top-right" */}
      {/*   hideProgressBar */}
      {/* /> */}
    </div>
  );
};

export default Router;
