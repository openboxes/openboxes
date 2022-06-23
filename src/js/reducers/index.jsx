import { localizeReducer } from 'react-localize-redux';
import { combineReducers } from 'redux';

import currenciesReducer from 'reducers/currenciesReducer';
import indicatorsReducer from 'reducers/indicatorsReducer';
import organizationsReducer from 'reducers/organizationsReducer';
import reasonCodesReducer from 'reducers/reasonCodesReducer';
import sessionReducer from 'reducers/sessionReducer';
import spinnerReducer from 'reducers/spinnerReducer';
import usersReducer from 'reducers/usersReducer';


const rootReducer = combineReducers({
  localize: localizeReducer,
  spinner: spinnerReducer,
  reasonCodes: reasonCodesReducer,
  users: usersReducer,
  session: sessionReducer,
  indicators: indicatorsReducer,
  currencies: currenciesReducer,
  organizations: organizationsReducer,
});

export default rootReducer;
