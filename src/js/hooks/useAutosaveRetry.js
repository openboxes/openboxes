import { useCallback, useEffect, useRef } from 'react';

import { useSelector } from 'react-redux';
import {
  getAutosaveMaxRetries,
  getAutosaveMaxRetryDelay,
  getAutosaveRetryDelay,
} from 'selectors';

/**
 * Retry policy of the autosave: exponential backoff with an upper delay limit and an
 * instant retry when the browser is back online. The caller passes `onRetry` (fired when the
 * backoff time passes or the connection returns). Defaults come from the backend config.
 *
 * @returns {{
 *   maxRetries: number,
 *   isNetworkError: Function,
 *   scheduleRetry: Function,
 *   cancelRetry: Function,
 * }} `maxRetries` - retry attempts per row for failures answered by the server.
 *    `isNetworkError(error)` - checks if a save failed because there was no connection;
 *    such failures retry without using up the `maxRetries` budget.
 *    `scheduleRetry(backoffLevel)` - (re)starts the retry timer with the delay for that level.
 *    `cancelRetry()` - stops the retry timer.
 */
const useAutosaveRetry = ({
  maxRetries,
  retryDelay,
  maxRetryDelay,
  // A plain thrown Error must not be treated as a network failure.
  // It would retry forever - so the check requires isAxiosError.
  isNetworkError = (error) => Boolean(error?.isAxiosError) && !error.response,
  onRetry,
}) => {
  const configMaxRetries = useSelector(getAutosaveMaxRetries);
  const configRetryDelay = useSelector(getAutosaveRetryDelay);
  const configMaxRetryDelay = useSelector(getAutosaveMaxRetryDelay);

  const timerRef = useRef(null);
  // Latest options for the stable callbacks below, read when the timer or event fires.
  const optionsRef = useRef({});
  optionsRef.current = {
    retryDelay: retryDelay ?? configRetryDelay,
    maxRetryDelay: maxRetryDelay ?? configMaxRetryDelay,
    onRetry,
  };

  const cancelRetry = useCallback(() => clearTimeout(timerRef.current), []);

  const scheduleRetry = useCallback((backoffLevel) => {
    clearTimeout(timerRef.current);
    // Delay is limited so endless network retries keep a steady pace instead of the delay
    // growing forever.
    const delay = Math.min(
      optionsRef.current.retryDelay * (2 ** (backoffLevel - 1)),
      optionsRef.current.maxRetryDelay,
    );
    timerRef.current = setTimeout(() => optionsRef.current.onRetry(), delay);
  }, []);

  // When the connection is back, retry right away instead of waiting out the current backoff.
  useEffect(() => {
    const onOnline = () => {
      clearTimeout(timerRef.current);
      optionsRef.current.onRetry();
    };
    window.addEventListener('online', onOnline);
    return () => {
      window.removeEventListener('online', onOnline);
      clearTimeout(timerRef.current);
    };
  }, []);

  return {
    maxRetries: maxRetries ?? configMaxRetries,
    isNetworkError,
    scheduleRetry,
    cancelRetry,
  };
};

export default useAutosaveRetry;
