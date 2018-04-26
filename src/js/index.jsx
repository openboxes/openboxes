import ReactDOM from 'react-dom';
import React from 'react';
import { BrowserRouter, Switch, Route } from 'react-router-dom';

import '../css/main.scss';

const App = () => (
  <div>React Component</div>
);

ReactDOM.render(
  <BrowserRouter>
    <Switch>
      <Route path="/**/stockMovement" component={App} />
    </Switch>
  </BrowserRouter>
  , document.getElementById('root'),
);
