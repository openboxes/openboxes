import { useEffect, useState } from 'react';

import queryString from 'query-string';

import { PRODUCT_API } from 'api/urls';
import apiClient from 'utils/apiClient';

const useLastCountedDate = (tableData, loading) => {
  const [lastCountedDateMap, setLastCountedDateMap] = useState({});

  const fetchLatestInventoryCountDate = async () => {
    if (tableData.data.length) {
      const res = await apiClient.get(`${PRODUCT_API}/getLatestInventoryCountDate`, {
        params: {
          productIds: tableData.data.map((row) => row.product.id),
        },
        paramsSerializer: (parameters) => queryString.stringify(parameters),
      });
      setLastCountedDateMap(res.data.data);
    }
  };

  useEffect(() => {
    if (!loading) {
      fetchLatestInventoryCountDate();
      return;
    }
    setLastCountedDateMap({});
  }, [loading]);

  return {
    lastCountedDateMap,
  };
};

export default useLastCountedDate;
