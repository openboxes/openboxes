import * as Sentry from "@sentry/react";

/**
 * Initialize Sentry for performance and error monitoring.
 */
const initializeSentry = () => {
    // If our custom DSN environment variable isn't defined, skip all frontend monitoring. This check is potentially
    // unnecessary, but avoids potential weirdness with environments that don't have Sentry configured on the frontend.
    if (!process.env.REACT_APP_SENTRY_DSN_FRONTEND) {
        return;
    }

    Sentry.init({
        // To disable Sentry, unset this DSN value. Note that unlike on the backend, Sentry with React will not
        // be able to discover the SENTRY_DSN, SENTRY_ENVIRONMENT environment variables, so we need to define our own.
        // https://docs.sentry.io/platforms/javascript/guides/react/configuration/options/#common-options
        dsn: process.env.REACT_APP_SENTRY_DSN_FRONTEND,
        environment: process.env.REACT_APP_SENTRY_ENVIRONMENT,

        integrations: [
            Sentry.httpClientIntegration(),  // Adds HTTP request/response body to errors
            Sentry.browserTracingIntegration(),  // Enables performance tracing
            Sentry.replayIntegration({
                // Capture all request bodies in the replay except those that contain sensitive info such as passwords.
                networkDetailAllowUrls: [window.location.origin],
                networkDetailDenyUrls: [/\/openboxes\/auth\/login/],
                // We don't mask any user input in our session replay videos because the only sensitive information
                // that we have is user passwords, which is already being hidden from display by the app. Everything
                // else is visible by admins.
                maskAllText: false,
                maskAllInputs: false,
                blockAllMedia: false,
            }),
        ],

        // For Distributed Tracing. Only enable traces to continue through openboxes app requests.
        tracePropagationTargets: ["localhost", /^\/openboxes\//],

        // Enables capturing headers and cookies in error traces.
        sendDefaultPii: true,

        // The ratio of all requests to capture for performance tracing. 1.0 is 100% of requests. Disable tracing
        // by setting a value of 0. We keep this number low (as recommended by Sentry) for performance reasons, but
        // it can be increased if we decide we need more data.
        tracesSampleRate: 0.1,

        // For Session Replay. We only capture session recordings if there is an error (which we record 100% of).
        replaysSessionSampleRate: 0,
        replaysOnErrorSampleRate: 1.0,
    });
}

export default initializeSentry;
