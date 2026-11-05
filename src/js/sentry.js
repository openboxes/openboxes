import initializeSentry from 'config/sentry';

/*
 * An entry point (that is bundled by webpack) for GSPs to be able to use Sentry. See
 * grails-app/views/common/_sentry.gsp. React pages initialize Sentry via src/js/index.jsx instead.
 */
initializeSentry();
