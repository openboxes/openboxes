import React, { lazy, Suspense, useMemo } from 'react';

import queryString from 'query-string';
import { useSelector } from 'react-redux';
import {
  BrowserRouter, Navigate, Route, Routes, useLocation,
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
import { CONTEXT_PATH, DASHBOARD_URL } from 'consts/applicationUrls';
import useConnectionListener from 'hooks/useConnectionListener';
import FlashScopeListenerWrapper from 'wrappers/FlashScopeListenerWrapper';

import 'react-toastify/dist/ReactToastify.css';
// TODO: Fix entering Inbound SM from list

const AsyncStockMovement = lazy(() => import('components/stock-movement-wizard/StockMovement'));

const AsyncStockMovementInbound = lazy(() => import('components/stock-movement-wizard/inboundV2/Inbound'));

const AsyncStockMovementCombinedShipments = lazy(() => import('components/stock-movement-wizard/StockMovementCombinedShipments'));

const AsyncStockMovementRequest = lazy(() => import('components/stock-movement-wizard/StockMovementRequest'));

const AsyncStockMovementVerifyRequest = lazy(() => import('components/stock-movement-wizard/StockMovementVerifyRequest'));

const AsyncReceiving = lazy(() => import('components/receivingV2/Receiving'));

// TODO: Remove this loader once the new receiving refactor is complete
//  (legacy PartialReceivingPage)
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

const StockMovementList = () => {
  const location = useLocation();
  const parsedSearchQuery = queryString.parse(location.search);
  const direction = parsedSearchQuery?.direction?.toUpperCase();
  switch (direction) {
    case 'INBOUND':
      return <AsyncStockMovementInboundList />;
    case 'OUTBOUND': {
      return (
        <AsyncStockMovementOutboundList sourceType={parsedSearchQuery?.sourceType?.toUpperCase()} />
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
      <BrowserRouter>
        <FlashScopeListenerWrapper>
          <Suspense fallback={<div style={{ textAlign: 'center', padding: '50px' }}>Loading...</div>}>
            <Routes>
              <Route element={<MainLayout />}>
                <Route path={`${CONTEXT_PATH}/purchaseOrder/list`} element={<AsyncPurchaseOrderList />} />
                <Route path={`${CONTEXT_PATH}/requisitionTemplate/list`} element={<AsyncStockList />} />
                <Route path={`${CONTEXT_PATH}/product/list`} element={<AsyncProductsList />} />
                <Route path={`${CONTEXT_PATH}/stockTransfer/list`} element={<AsyncStockTransferList />} />
                <Route path={`${CONTEXT_PATH}/productSupplier/list`} element={<AsyncProductSupplierList />} />
                <Route path={`${CONTEXT_PATH}/productSupplier/create/:productSupplierId?`} element={<AsyncProductSupplierCreatePage />} />
                <Route path={`${CONTEXT_PATH}/dashboard/:configId?`} element={<Dashboard />} />
                <Route path={`${CONTEXT_PATH}/putAway/create/:putAwayId?`} element={<AsyncPutAwayMainPage />} />
                <Route path={`${CONTEXT_PATH}/stockMovement/list`} element={<StockMovementList />} />
                <Route path={`${CONTEXT_PATH}/stockMovement/createOutbound/:stockMovementId?`} element={<AsyncStockMovement />} />
                <Route path={`${CONTEXT_PATH}/stockMovement/importOutboundStockMovement`} element={<AsyncOutboundImport />} />
                <Route path={`${CONTEXT_PATH}/report/expirationHistoryReport`} element={<AsyncExpirationHistoryReport />} />
                <Route path={`${CONTEXT_PATH}/inventory/reorderReport`} element={<AsyncReorderReport />} />
                <Route path={`${CONTEXT_PATH}/inventory/cycleCount/count`} element={<AsyncCycleCountCountStep />} />
                <Route path={`${CONTEXT_PATH}/inventory/cycleCount/resolve`} element={<AsyncCycleCountResolveStep />} />
                <Route path={`${CONTEXT_PATH}/inventory/cycleCount/reporting`} element={<AsyncCycleCountReporting />} />
                <Route path={`${CONTEXT_PATH}/inventory/cycleCount`} element={<AsyncCycleCount />} />
                <Route path={`${CONTEXT_PATH}/stockMovement/createInbound/:stockMovementId?`} element={<AsyncStockMovementInbound />} />
                <Route path={`${CONTEXT_PATH}/stockMovement/createCombinedShipments/:stockMovementId?`} element={<AsyncStockMovementCombinedShipments />} />
                <Route path={`${CONTEXT_PATH}/stockMovement/createRequest/:stockMovementId?`} element={<AsyncStockMovementRequest />} />
                <Route path={`${CONTEXT_PATH}/stockMovement/verifyRequest/:stockMovementId?`} element={<AsyncStockMovementVerifyRequest />} />
                <Route path={`${CONTEXT_PATH}/stockMovement/create/:stockMovementId?`} element={<AsyncStockMovement />} />
                <Route path={`${CONTEXT_PATH}/receiving/create/:shipmentId`} element={<AsyncReceiving />} />
                {/* TODO: Remove this route once the new receiving refactor is complete
                (legacy PartialReceivingPage) */}
                <Route path={`${CONTEXT_PATH}/partialReceiving/create/:shipmentId`} element={<AsyncReceivingPage />} />
                <Route path={`${CONTEXT_PATH}/stocklistManagement/index/:productId?`} element={<AsyncManagement />} />
                <Route path={`${CONTEXT_PATH}/invoice/create/:invoiceId?`} element={<AsyncInvoice />} />
                <Route path={`${CONTEXT_PATH}/invoice/list`} element={<AsyncInvoiceList />} />
                <Route path={`${CONTEXT_PATH}/stockTransfer/create/:stockTransferId?`} element={<AsyncStockTransfer />} />
                <Route path={`${CONTEXT_PATH}/stockTransfer/createOutboundReturn/:outboundReturnId?`} element={<AsyncOutboundReturns />} />
                <Route path={`${CONTEXT_PATH}/stockTransfer/createInboundReturn/:inboundReturnId?`} element={<AsyncInboundReturns />} />
                <Route path={`${CONTEXT_PATH}/replenishment/create/:replenishmentId?`} element={<AsyncReplenishment />} />
                <Route path={`${CONTEXT_PATH}/productsConfiguration/index`} element={<AsyncProductsConfiguration />} />
                <Route path={`${CONTEXT_PATH}/locationsConfiguration/create/:locationId?`} element={<AsyncLocationsConfiguration />} />
                <Route path={`${CONTEXT_PATH}/locationsConfiguration/upload`} element={<AsyncImportLocations />} />
                <Route path="*" element={<Dashboard />} />
              </Route>
              <Route path={`${CONTEXT_PATH}/locationsConfiguration/index`} element={<AsyncWelcomePage />} />
              <Route path={`${CONTEXT_PATH}/loadData/index`} element={<AsyncLoadDataPage />} />
              <Route path={`${CONTEXT_PATH}/resettingInstanceInfo/index`} element={<AsyncResetInstancePage />} />
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
