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
// eslint-disable-next-line import/extensions
import '@fontsource/inter';

import initializeSentry from './config/sentry';

import 'bootstrap/dist/css/bootstrap.css';
import 'font-awesome/css/font-awesome.min.css';
// eslint-disable-next-line import/no-unresolved
import 'css/main.scss';
import 'react-tippy/dist/tippy.css';

library.add(fab, fas);
// eslint-disable-next-line camelcase,no-undef
__webpack_public_path__ = `${window.CONTEXT_PATH}/static/webpack/`;

// Initialize Sentry as early as possible so that it can capture startup errors as well.
initializeSentry();

ReactDOM.render(
  <Provider store={store}>
    <LocalizeProvider store={store}>
      <MetaTags>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
      </MetaTags>
      <MainRouter />
    </LocalizeProvider>
  </Provider>,
  document.getElementById('root'),
);
