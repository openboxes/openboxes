import { useEffect } from 'react';

import queryString from 'query-string';
import { useLocation, useNavigate } from 'react-router-dom';

import useQueryParams from 'hooks/useQueryParams';

const useSwitchTabs = ({ defaultTab }) => {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const currentQueryParams = useQueryParams();

  const switchTab = (tab, onSwitchTab) => {
    const updatedQueryParams = {
      ...currentQueryParams,
      tab,
    };
    const stringifiedQueryParams = queryString.stringify(updatedQueryParams);
    navigate({ pathname, search: stringifiedQueryParams });
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
