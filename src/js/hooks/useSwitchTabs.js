import { useEffect } from 'react';

import queryString from 'query-string';
import { useHistory, useLocation } from 'react-router-dom';

import useQueryParams from 'hooks/useQueryParams';

const useSwitchTabs = ({ defaultTab }) => {
  const history = useHistory();
  const { pathname } = useLocation();
  const currentQueryParams = useQueryParams();

  const switchTab = (tab, onSwitchTab) => {
    const updatedQueryParams = {
      ...currentQueryParams,
      tab,
    };
    const stringifiedQueryParams = queryString.stringify(updatedQueryParams);
    history.push({ pathname, search: stringifiedQueryParams });
    onSwitchTab?.();
  };

  useEffect(() => {
    // If we don't have tab query param,
    // we want to display the default tab by default
    if (!currentQueryParams.tab) {
      switchTab(defaultTab);
    }
  }, []);

  return {
    switchTab,
  };
};

export default useSwitchTabs;
