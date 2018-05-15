import ReactDOM from 'react-dom';
import React from 'react';
import { Provider } from 'react-redux';
import { createStore, applyMiddleware } from 'redux';
import { BrowserRouter, Switch } from 'react-router-dom';
import ReduxPromise from 'redux-promise';

import 'bootstrap/dist/js/bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import '../css/main.scss';

import rootReducer from './reducers';
import StockMovement from './components/StockMovement';
import MainLayoutRoute from './components/Layout/MainLayoutRoute';

const createStoreWithMiddleware = applyMiddleware(ReduxPromise)(createStore);
const store = createStoreWithMiddleware(rootReducer);

ReactDOM.render(
  <Provider store={store}>
    <BrowserRouter>
      <Switch>
        <MainLayoutRoute path="/**/stockMovement" component={StockMovement} />
      </Switch>
    </BrowserRouter>
  </Provider>
  , document.getElementById('root'),
);
