import ReactDOM from 'react-dom';
import React from 'react';
import { BrowserRouter, Switch, Route } from 'react-router-dom';

import App from './components/app';
import '../css/main.scss';

ReactDOM.render(
  <BrowserRouter>
    <Switch>
      <Route path="/**/stockMovement" component={App} />
    </Switch>
  </BrowserRouter>
  , document.getElementById('root'),
);
