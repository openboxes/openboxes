import { combineReducers } from 'redux';
import { reducer as formReducer } from 'redux-form';
import { localeReducer } from 'react-localize-redux';


const rootReducer = combineReducers({
  form: formReducer,
  locale: localeReducer,
});

export default rootReducer;
