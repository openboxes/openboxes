import React from 'react';

import { library } from '@fortawesome/fontawesome-svg-core';
import { fab } from '@fortawesome/free-brands-svg-icons';
import { fas } from '@fortawesome/free-solid-svg-icons';
import ReactDOM from 'react-dom';
import { LocalizeProvider } from 'react-localize-redux';
import MetaTags from 'react-meta-tags';
import Provider from 'react-redux/es/components/Provider';
// eslint-disable-next-line import/extensions,import/no-unresolved
import MainRouter from 'src/MainRouter';
import store from 'store';

import 'bootstrap/dist/js/bootstrap';

import 'bootstrap/dist/css/bootstrap.css';
import 'font-awesome/css/font-awesome.min.css';
// eslint-disable-next-line import/no-unresolved
import 'css/main.scss';

library.add(fab, fas);
// eslint-disable-next-line
__webpack_public_path__ = window.CONTEXT_PATH + '/assets/';

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

