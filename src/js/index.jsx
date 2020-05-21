import ReactDOM from 'react-dom';
import React from 'react';
import Provider from 'react-redux/es/components/Provider';
import { LocalizeProvider } from 'react-localize-redux';
import MetaTags from 'react-meta-tags';

import 'bootstrap/dist/js/bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'font-awesome/css/font-awesome.min.css';
import '../css/main.scss';

import store from './store';
import MainRouter from './MainRouter';

ReactDOM.render(
  <Provider store={store}>
    <LocalizeProvider store={store}>
      <MetaTags>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
      </MetaTags>
      <MainRouter />
    </LocalizeProvider>
  </Provider>
  , document.getElementById('root'),
);

