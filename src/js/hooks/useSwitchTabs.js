import { useEffect } from 'react';

import queryString from 'query-string';
import { useLocation, useNavigate } from 'react-router-dom';

import useQueryParams from 'hooks/useQueryParams';

const useSwitchTabs = ({ defaultTab }) => {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const currentQueryParams = useQueryParams();

  const getTabLocation = (tab) => {
    const updatedQueryParams = {
      ...currentQueryParams,
      tab,
    };
    const stringifiedQueryParams = queryString.stringify(updatedQueryParams);
    return { pathname, search: stringifiedQueryParams };
  };

  const switchTab = (tab, onSwitchTab) => {
    navigate(getTabLocation(tab));
    onSwitchTab?.();
  };

  useEffect(() => {
    // If we don't have tab query param,
    // we want to display the default tab by default.
    // It's a redirect, so it replaces the history entry instead of pushing a new one,
    // otherwise the browser back button would land on the URL without the tab
    if (!currentQueryParams.tab) {
      navigate(getTabLocation(defaultTab), { replace: true });
    }
  }, []);

  return {
    switchTab,
  };
};

export default useSwitchTabs;
