import { useEffect, useState } from 'react';

import productApi from 'api/services/ProductApi';

const useLastCountedDate = (tableData, loading) => {
  const [lastCountedDateMap, setLastCountedDateMap] = useState({});

  const fetchLatestInventoryCountDate = async () => {
    if (tableData.data.length) {
      const productIds = tableData.data.map((row) => row.product.id);
      const lastCountedDateResponse = await productApi.getLatestInventoryCountDate(productIds);
      setLastCountedDateMap(lastCountedDateResponse.data.data);
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
