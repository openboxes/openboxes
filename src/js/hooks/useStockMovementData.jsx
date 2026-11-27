import { useCallback, useEffect, useState } from 'react';

import StockMovementApi from 'api/services/StockMovementApi';

const useStockMovementData = (stockMovementId) => {
  const [stockMovement, setStockMovement] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = useCallback(async () => {
    if (!stockMovementId) return;
    setLoading(true);
    setError(null);
    try {
      const response = await StockMovementApi.getStockMovementById(stockMovementId);
      setStockMovement(response.data.data);
    } catch (err) {
      console.error('[useStockMovementData] error:', err);
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [stockMovementId]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return {
    stockMovement, loading, error, refetch: fetchData,
  };
};

export default useStockMovementData;
