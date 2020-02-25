import { combineReducers } from 'redux';
import { localizeReducer } from 'react-localize-redux';
import spinnerReducer from './spinnerReducer';
import usersReducer from './usersReducer';
import reasonCodesReducer from './reasonCodesReducer';
import sessionReducer from './sessionReducer';
import indicatorsReducer from './indicatorsReducer';

const rootReducer = combineReducers({
  localize: localizeReducer,
  spinner: spinnerReducer,
  reasonCodes: reasonCodesReducer,
  users: usersReducer,
  session: sessionReducer,
  indicators: indicatorsReducer,
});

export default rootReducer;
