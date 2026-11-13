import path from 'node:path';

import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

import fixDependencyImportsPlugin from './vite-plugins/fixDependencyImportsPlugin.mjs';
import grailsGspPlugin from './vite-plugins/grailsGspPlugin.mjs';

const ROOT = path.resolve(import.meta.dirname, 'src');
const SRC = path.resolve(ROOT, 'js');
const ENTRY = path.resolve(SRC, 'index.jsx');
const SENTRY_ENTRY = path.resolve(SRC, 'sentry.js');
const VENDOR_CSS = path.resolve(ROOT, 'css/vendor.css');
const OUTPUT = path.resolve(ROOT, 'main/webapp/dist');
const GRAILS_VIEWS = path.resolve(import.meta.dirname, 'grails-app/views');

const CONTEXT_PATH = '${util.ConfigHelper.contextPath}';
const SENTRY_CONFIG = '${raw(org.pih.warehouse.monitoring.SentryConfigResolver.getWebConfigJson(session.user, session.warehouse))}';

export default defineConfig({
  base: './',
  plugins: [
    react(),
    fixDependencyImportsPlugin(),
    grailsGspPlugin({
      entry: ENTRY,
      vendorCss: VENDOR_CSS,
      templatePath: path.resolve(ROOT, 'assets/grails-template.html'),
      pages: [
        {
          output: path.resolve(GRAILS_VIEWS, 'common/react.gsp'),
          params: {
            contextPath: CONTEXT_PATH,
            sentryConfig: SENTRY_CONFIG,
            receivingIfStatement: '',
          },
        },
        {
          output: path.resolve(GRAILS_VIEWS, 'partialReceiving/create.gsp'),
          params: {
            contextPath: CONTEXT_PATH,
            sentryConfig: SENTRY_CONFIG,
            receivingIfStatement: '<g:if test="${!params.id}">'
              + 'You can access the Partial Receiving feature through the details page for an inbound shipment.'
              + '</g:if>',
          },
        },
      ],
      // A standalone Sentry bundle for GSPs, see grails-app/views/common/_sentry.gsp
      scriptPartials: [
        {
          entry: SENTRY_ENTRY,
          output: path.resolve(GRAILS_VIEWS, 'common/_sentryBundle.gsp'),
        },
      ],
    }),
  ],
  resolve: {
    alias: {
      root: ROOT,
      src: SRC,
      components: path.resolve(SRC, 'components'),
      config: path.resolve(SRC, 'config'),
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
    chunkSizeWarningLimit: 1000,
    rollupOptions: {
      input: {
        index: ENTRY,
        sentry: SENTRY_ENTRY,
        vendor: VENDOR_CSS,
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    environmentOptions: {
      jsdom: { url: 'http://localhost/' },
    },
    include: ['src/js/**/*.test.{js,jsx}'],
    setupFiles: ['src/js/tests/setupTests.js'],
  },
});
