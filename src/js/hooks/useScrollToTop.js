import { useLayoutEffect } from 'react';

/**
 * A custom React hook that scrolls the window to the top of the page
 * when the component mounts.
 */
const useScrollToTop = (deps = []) => {
  useLayoutEffect(() => {
    window.scrollTo(0, 0);
  }, deps);
};

export default useScrollToTop;
