import ReactDOM from 'react-dom';
import React from 'react';
import { BrowserRouter, Switch } from 'react-router-dom';

import 'bootstrap/dist/js/bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import '../css/main.scss';

import StockMovement from './components/StockMovement';
import MainLayoutRoute from './components/Layout/MainLayoutRoute';

ReactDOM.render(
  <BrowserRouter>
    <Switch>
      <MainLayoutRoute path="/**/stockMovement" component={StockMovement} />
    </Switch>
  </BrowserRouter>
  , document.getElementById('root'),
);
