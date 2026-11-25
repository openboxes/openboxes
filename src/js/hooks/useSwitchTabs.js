import { useEffect } from 'react';

import queryString from 'query-string';
import { useHistory, useLocation } from 'react-router-dom';

import useQueryParams from 'hooks/useQueryParams';

const useSwitchTabs = ({ defaultTab }) => {
  const history = useHistory();
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
    history.push(getTabLocation(tab));
    onSwitchTab?.();
  };

  useEffect(() => {
    // If we don't have tab query param,
    // we want to display the default tab by default.
    // It's a redirect, so it replaces the history entry instead of pushing a new one,
    // otherwise the browser back button would land on the URL without the tab
    if (!currentQueryParams.tab) {
      history.replace(getTabLocation(defaultTab));
    }
  }, []);

  return {
    switchTab,
  };
};

export default useSwitchTabs;
