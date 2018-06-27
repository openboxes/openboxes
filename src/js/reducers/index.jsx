import { combineReducers } from 'redux';
import { reducer as formReducer } from 'redux-form';
import { localeReducer } from 'react-localize-redux';
import spinnerReducer from './spinnerReducer';


const rootReducer = combineReducers({
  form: formReducer,
  locale: localeReducer,
  spinner: spinnerReducer,
});

export default rootReducer;
