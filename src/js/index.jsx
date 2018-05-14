import ReactDOM from 'react-dom';
import React from 'react';
import { BrowserRouter, Switch } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap/dist/js/bootstrap';

import StockMovement from './components/StockMovement';
import MainLayoutRoute from './components/Layout/MainLayoutRoute';
import '../css/main.scss';

ReactDOM.render(
  <BrowserRouter>
    <Switch>
      <MainLayoutRoute path="/**/stockMovement" component={StockMovement} />
    </Switch>
  </BrowserRouter>
  , document.getElementById('root'),
);
