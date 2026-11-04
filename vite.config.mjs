import path from 'node:path';

import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

import grailsGspPlugin from './vite-plugins/grailsGspPlugin.mjs';

const ROOT = path.resolve(import.meta.dirname, 'src');
const SRC = path.resolve(ROOT, 'js');
const ENTRY = path.resolve(SRC, 'index.jsx');
const OUTPUT = path.resolve(ROOT, 'main/webapp/webpack');
const GRAILS_VIEWS = path.resolve(import.meta.dirname, 'grails-app/views');

const SENTRY_ENV_VARIABLES = [
  'REACT_APP_WEB_SENTRY_DSN',
  'REACT_APP_SENTRY_ENVIRONMENT',
  'REACT_APP_WEB_SENTRY_TRACES_SAMPLE_RATE',
  'REACT_APP_WEB_SENTRY_REPLAYS_SAMPLE_RATE',
  'REACT_APP_WEB_SENTRY_REPLAYS_ERROR_SAMPLE_RATE',
];

export default defineConfig({
  base: './',
  plugins: [
    react(),
    grailsGspPlugin({
      entry: ENTRY,
      templatePath: path.resolve(ROOT, 'assets/grails-template.html'),
      pages: [
        {
          output: path.resolve(GRAILS_VIEWS, 'common/react.gsp'),
          params: {
            contextPath: '${util.ConfigHelper.contextPath}',
            receivingIfStatement: '',
          },
        },
        {
          output: path.resolve(GRAILS_VIEWS, 'partialReceiving/create.gsp'),
          params: {
            contextPath: '${util.ConfigHelper.contextPath}',
            receivingIfStatement: '<g:if test="${!params.id}">'
              + 'You can access the Partial Receiving feature through the details page for an inbound shipment.'
              + '</g:if>',
          },
        },
      ],
    }),
  ],
  define: Object.fromEntries(SENTRY_ENV_VARIABLES.map((name) => [
    `process.env.${name}`,
    JSON.stringify(process.env[name]),
  ])),
  resolve: {
    alias: {
      root: ROOT,
      src: SRC,
      components: path.resolve(SRC, 'components'),
      hooks: path.resolve(SRC, 'hooks'),
      reducers: path.resolve(SRC, 'reducers'),
      selectors: path.resolve(SRC, 'selectors'),
      schemes: path.resolve(SRC, 'schemes'),
      middlewares: path.resolve(SRC, 'middlewares'),
      actions: path.resolve(SRC, 'actions'),
      consts: path.resolve(SRC, 'consts'),
      tests: path.resolve(SRC, 'tests'),
      utils: path.resolve(SRC, 'utils'),
      templates: path.resolve(SRC, 'templates'),
      store: path.resolve(SRC, 'store'),
      css: path.resolve(ROOT, 'css'),
      api: path.resolve(SRC, 'api'),
      wrappers: path.resolve(SRC, 'wrappers'),
    },
    extensions: ['.js', '.jsx'],
  },
  css: {
    preprocessorOptions: {
      scss: {
        loadPaths: [import.meta.dirname],
      },
    },
  },
  build: {
    outDir: OUTPUT,
    emptyOutDir: true,
    assetsDir: '',
    sourcemap: true,
    rollupOptions: {
      input: ENTRY,
    },
  },
});
