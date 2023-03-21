import { localizeReducer } from 'react-localize-redux';
import { combineReducers } from 'redux';

import connectionReducer from 'reducers/connectionReducer';
import currenciesReducer from 'reducers/currenciesReducer';
import filterFormReducer from 'reducers/filterFormReducer';
import indicatorsReducer from 'reducers/indicatorsReducer';
import invoiceReducer from 'reducers/invoiceReducer';
import organizationsReducer from 'reducers/organizationsReducer';
import purchaseOrderReducer from 'reducers/purchaseOrderReducer';
import reasonCodesReducer from 'reducers/reasonCodesReducer';
import requisitionStatusCodes from 'reducers/requisitionStatusCodes';
import sessionReducer from 'reducers/sessionReducer';
import shipmentStatusCodes from 'reducers/shipmentStatusCodes';
import spinnerReducer from 'reducers/spinnerReducer';
import stockMovementCommonReducer from 'reducers/stockMovementCommonReducer';
import stockMovementDraftReducer from 'reducers/stockMovementDraftReducer';
import stockTransferReducer from 'reducers/stockTransferReducer';
import usersReducer from 'reducers/usersReducer';


const rootReducer = combineReducers({
  localize: localizeReducer,
  spinner: spinnerReducer,
  connection: connectionReducer,
  reasonCodes: reasonCodesReducer,
  users: usersReducer,
  session: sessionReducer,
  indicators: indicatorsReducer,
  currencies: currenciesReducer,
  organizations: organizationsReducer,
  purchaseOrder: purchaseOrderReducer,
  invoices: invoiceReducer,
  shipmentStatuses: shipmentStatusCodes,
  requisitionStatuses: requisitionStatusCodes,
  stockTransfer: stockTransferReducer,
  filterForm: filterFormReducer,
  stockMovementDraft: stockMovementDraftReducer,
  stockMovementCommon: stockMovementCommonReducer,
});

export default rootReducer;
