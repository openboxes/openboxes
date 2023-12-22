import { useEffect } from 'react';

import queryString from 'query-string';
import { useHistory, useLocation } from 'react-router-dom';

import { DETAILS_TAB } from 'consts/productSupplierList';
import useQueryParams from 'hooks/useQueryParams';

const useProductSupplierTabs = () => {
  const history = useHistory();
  const { pathname } = useLocation();
  const currentQueryParams = useQueryParams();

  const switchTab = (tab) => {
    const updatedQueryParams = {
      ...currentQueryParams,
      tab,
    };
    const stringifiedQueryParams = queryString.stringify(updatedQueryParams);
    history.push({ pathname, search: stringifiedQueryParams });
  };

  useEffect(() => {
    // If we don't have tab query param,
    // we want to display the detalis tab by default
    if (!currentQueryParams.tab) {
      switchTab(DETAILS_TAB);
    }
  }, []);

  return {
    switchTab,
  };
};

export default useProductSupplierTabs;
