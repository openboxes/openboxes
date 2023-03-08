import { useEffect, useState } from 'react';

import ReactGA from 'react-ga';
import { useLocation } from 'react-router-dom';

const useGaTracker = () => {
  const location = useLocation();
  const [initialized, setInitialized] = useState(false);

  useEffect(() => {
    if (!initialized && window.WEB_PROPERTY_ID) {
      ReactGA.initialize(window.WEB_PROPERTY_ID);
      setInitialized(true);
    }
  }, []);

  useEffect(() => {
    if (initialized) {
      ReactGA.pageview(location.pathname + location.search);
    }
  }, [initialized, location.pathname, location.search]);
};

export default useGaTracker;
