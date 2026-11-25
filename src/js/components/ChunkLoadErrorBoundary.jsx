import React from 'react';

import PropTypes from 'prop-types';
import { ErrorBoundary } from 'react-error-boundary';
import { RiErrorWarningLine, RiRefreshLine } from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

// Failed dynamic imports reject with a native TypeError (message differs per browser),
// failed CSS preloads with an Error thrown by Vite's preload helper
const CHUNK_LOAD_ERROR_MESSAGES = [
  'Failed to fetch dynamically imported module', // Chromium
  'error loading dynamically imported module', // Firefox
  'Importing a module script failed', // Safari
  'Unable to preload CSS', // Vite
];

export const isChunkLoadError = (error) =>
  CHUNK_LOAD_ERROR_MESSAGES.some((message) => error?.message?.includes(message));

const ChunkLoadErrorFallback = ({ error }) => {
  if (!isChunkLoadError(error)) {
    throw error;
  }

  return (
    <div className="chunk-load-error">
      <RiErrorWarningLine className="chunk-load-error__icon" />
      <span className="chunk-load-error__title">
        <Translate
          id="react.default.error.chunkLoad.title.label"
          defaultMessage="This page could not be loaded"
        />
      </span>
      <span className="chunk-load-error__description">
        <Translate
          id="react.default.error.chunkLoad.description.label"
          defaultMessage="It may have been updated in the meantime or the connection was lost. Reload the page to try again."
        />
      </span>
      <Button
        label="react.default.button.refresh.label"
        defaultLabel="Reload"
        variant="primary"
        StartIcon={<RiRefreshLine />}
        onClick={() => window.location.reload()}
      />
    </div>
  );
};

const ChunkLoadErrorBoundary = ({ children, resetKeys = [] }) => (
  <ErrorBoundary FallbackComponent={ChunkLoadErrorFallback} resetKeys={resetKeys}>
    {children}
  </ErrorBoundary>
);

export default ChunkLoadErrorBoundary;

ChunkLoadErrorFallback.propTypes = {
  error: PropTypes.shape({
    message: PropTypes.string,
  }).isRequired,
};

ChunkLoadErrorBoundary.propTypes = {
  children: PropTypes.node.isRequired,
  // eslint-disable-next-line react/forbid-prop-types
  resetKeys: PropTypes.arrayOf(PropTypes.any),
};
