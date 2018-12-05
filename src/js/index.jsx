import ReactDOM from 'react-dom';
import React from 'react';
import { Provider } from 'react-redux';
import { createStore, applyMiddleware } from 'redux';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';
import { initialize, addTranslationForLanguage } from 'react-localize-redux';

import 'bootstrap/dist/js/bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'font-awesome/css/font-awesome.min.css';
import '../css/main.scss';

import { fetchTranslations } from './actions';
import Router from './components/Router';
import rootReducer from './reducers';
import en from './en';
import fr from './fr';

const createStoreWithMiddleware = applyMiddleware(ReduxPromise, reduxThunk)(createStore);
const store = createStoreWithMiddleware(rootReducer);

const languages = [
  { name: 'English', code: 'en' },
  { name: 'French', code: 'fr' },
];
store.dispatch(initialize(languages));

store.dispatch(addTranslationForLanguage(en, 'en'));
store.dispatch(addTranslationForLanguage(fr, 'fr'));

store.dispatch(fetchTranslations('en'));

ReactDOM.render(
  <Provider store={store}>
    <Router />
  </Provider>
  , document.getElementById('root'),
);
