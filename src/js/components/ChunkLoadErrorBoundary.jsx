import React from 'react';

import PropTypes from 'prop-types';
import { ErrorBoundary } from 'react-error-boundary';
import { RiErrorWarningLine, RiRefreshLine } from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

const isChunkLoadError = (error) =>
  error?.name === 'ChunkLoadError' || error?.code === 'CSS_CHUNK_LOAD_FAILED';

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
    name: PropTypes.string,
    code: PropTypes.string,
  }).isRequired,
};

ChunkLoadErrorBoundary.propTypes = {
  children: PropTypes.node.isRequired,
  resetKeys: PropTypes.arrayOf(PropTypes.any),
};

ChunkLoadErrorBoundary.defaultProps = {
  resetKeys: [],
};
