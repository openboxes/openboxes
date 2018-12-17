import ReactDOM from 'react-dom';
import React from 'react';
import Provider from 'react-redux/es/components/Provider';
import { LocalizeProvider } from 'react-localize-redux';
import { applyMiddleware, createStore } from 'redux';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';

import 'bootstrap/dist/js/bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'font-awesome/css/font-awesome.min.css';
import '../css/main.scss';

import MainRouter from './MainRouter';
import rootReducer from './reducers';


const createStoreWithMiddleware = applyMiddleware(ReduxPromise, reduxThunk)(createStore);
const store = createStoreWithMiddleware(rootReducer);

ReactDOM.render(
  <Provider store={store}>
    <LocalizeProvider store={store}>
      <MainRouter />
    </LocalizeProvider>
  </Provider>
  , document.getElementById('root'),
);

