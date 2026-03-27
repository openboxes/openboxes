import React from 'react';

import { library } from '@fortawesome/fontawesome-svg-core';
import { fab } from '@fortawesome/free-brands-svg-icons';
import { fas } from '@fortawesome/free-solid-svg-icons';
import { createRoot } from 'react-dom/client';
import { Helmet, HelmetProvider } from 'react-helmet-async';
import { LocalizeProvider } from 'react-localize-redux';
import { Provider } from 'react-redux';
// eslint-disable-next-line import/extensions,import/no-unresolved
import MainRouter from 'src/MainRouter';
import store from 'store';

import 'bootstrap/dist/js/bootstrap';
// eslint-disable-next-line import/extensions
import '@fontsource/inter';

import initializeSentry from './config/sentry';

import 'react-tooltip/dist/react-tooltip.css';
import 'bootstrap/dist/css/bootstrap.css';
import 'font-awesome/css/font-awesome.min.css';
// eslint-disable-next-line import/no-unresolved
import 'css/main.scss';

library.add(fab, fas);
// eslint-disable-next-line camelcase,no-undef
__webpack_public_path__ = `${window.CONTEXT_PATH}/static/webpack/`;

// Initialize Sentry as early as possible so that it can capture startup errors as well.
initializeSentry();

const container = document.getElementById('root');
const root = createRoot(container);

root.render(
  <Provider store={store}>
    <LocalizeProvider store={store}>
      <HelmetProvider>
        <Helmet>
          <meta name="viewport" content="width=device-width, initial-scale=1" />
        </Helmet>
        <MainRouter />
      </HelmetProvider>
    </LocalizeProvider>
  </Provider>,
);
