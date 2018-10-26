import { combineReducers } from 'redux';
import { localeReducer } from 'react-localize-redux';
import spinnerReducer from './spinnerReducer';
import usersReducer from './usersReducer';
import reasonCodesReducer from './reasonCodesReducer';
import locationsReducer from './locationsReducer';

const rootReducer = combineReducers({
  locale: localeReducer,
  spinner: spinnerReducer,
  reasonCodes: reasonCodesReducer,
  users: usersReducer,
  location: locationsReducer,
});

export default rootReducer;
