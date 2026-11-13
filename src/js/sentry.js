import initializeSentry from 'config/sentry';

/*
 * An entry point (bundled by Vite, see vite.config.mjs) for GSPs to be able to use Sentry. See
 * grails-app/views/common/_sentry.gsp. React pages initialize Sentry via src/js/index.jsx instead.
 */
initializeSentry();
