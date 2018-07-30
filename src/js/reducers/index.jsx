import { combineReducers } from 'redux';
import { reducer as formReducer } from 'redux-form';
import { localeReducer } from 'react-localize-redux';
import spinnerReducer from './spinnerReducer';
import locationsReducer from './locationsReducer';
import usersReducer from './usersReducer';
import reasonCodesReducer from './reasonCodesReducer';


const rootReducer = combineReducers({
  form: formReducer,
  locale: localeReducer,
  spinner: spinnerReducer,
  locations: locationsReducer,
  reasonCodes: reasonCodesReducer,
  users: usersReducer,
});

export default rootReducer;
