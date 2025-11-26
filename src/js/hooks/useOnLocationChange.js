import { useEffect, useRef } from 'react';

import { useSelector } from 'react-redux';
import { getCurrentLocation } from 'selectors';

/**
 * A reusable hook that triggers a callback whenever the current location changes.
 *
 * @param {function} callback
 */
const useOnLocationChange = (callback) => {
  const currentLocation = useSelector(getCurrentLocation);
  const previousLocation = useRef(currentLocation?.id);

  useEffect(() => {
    // This if statement ensure that we run the callback only when the location actually changes,
    // not on the initial render.
    if (previousLocation.current !== currentLocation?.id) {
      callback?.();
      // Update the reference to the current location for future checks
      previousLocation.current = currentLocation?.id;
    }
  }, [currentLocation?.id]);
};

export default useOnLocationChange;
