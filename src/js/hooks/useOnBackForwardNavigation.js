import { useEffect, useRef } from 'react';

import { useHistory } from 'react-router-dom';

/**
 * A reusable hook that triggers a callback whenever the user navigates with the browser's
 * back/forward buttons while the component is mounted - both to another history entry
 * of the app (with a different path or query string) and when the browser restores the page
 * from its back/forward cache.
 *
 * @param {function} callback
 */
const useOnBackForwardNavigation = (callback) => {
  const history = useHistory();
  // Keep the latest callback, so the listeners don't have to be re-registered on every render
  const callbackRef = useRef(callback);
  callbackRef.current = callback;

  const previousLocationRef = useRef(history.location);

  useEffect(() => {
    previousLocationRef.current = history.location;
    return history.listen((location, action) => {
      // Clicking an in-page anchor (e.g. <a href="#">) is reported as a POP as well,
      // so only a POP that changes the path or the query string counts as back/forward navigation
      const isSamePathAndSearch = location.pathname === previousLocationRef.current.pathname
        && location.search === previousLocationRef.current.search;
      previousLocationRef.current = location;
      if (action === 'POP' && !isSamePathAndSearch) {
        callbackRef.current?.();
      }
    });
  }, [history]);

  // Covers coming back to the page after leaving it for another document instead of
  // a router navigation, e.g. a plain <a href> to a GSP page like the stock card.
  // The browser may then restore the page from its back/forward cache as it was left
  // (with the old state and data), without a reload and without a POP in the router history.
  useEffect(() => {
    const handlePageShow = (event) => {
      // persisted is true only when the page is restored from the back/forward cache
      if (event.persisted) {
        callbackRef.current?.();
      }
    };
    window.addEventListener('pageshow', handlePageShow);
    return () => window.removeEventListener('pageshow', handlePageShow);
  }, []);
};

export default useOnBackForwardNavigation;
