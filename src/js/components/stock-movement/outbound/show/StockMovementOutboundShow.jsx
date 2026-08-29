import React from 'react';

import { useParams } from 'react-router-dom';

import StockMovementOutboundShowDetails from 'components/stock-movement/outbound/show/StockMovementOutboundShowDetails';
import StockMovementOutboundShowHeader from 'components/stock-movement/outbound/show/StockMovementOutboundShowHeader';
import StockMovementOutboundShowLineItems from 'components/stock-movement/outbound/show/StockMovementOutboundShowLineItems';
import useStockMovementData from 'hooks/useStockMovementData';
import useTranslation from 'hooks/useTranslation';

import 'components/stock-movement/outbound/show/StockMovementOutboundShow.scss';

const StockMovementOutboundShow = () => {
  useTranslation('stockMovement', 'reactTable');
  const { stockMovementId } = useParams();
  const { stockMovement, loading, error } = useStockMovementData(stockMovementId);

  if (loading) {
    return <div className="text-center p-4">Loading...</div>;
  }

  if (error) {
    return (
      <div className="text-center p-4 text-danger">
        Failed to load stock movement.
      </div>
    );
  }

  if (!stockMovement) {
    return null;
  }

  return (
    <div className="list-page-main">
      <StockMovementOutboundShowHeader stockMovement={stockMovement} />
      <div className="stock-movement-show-content">
        <StockMovementOutboundShowDetails stockMovement={stockMovement} />
        <StockMovementOutboundShowLineItems
          lineItems={stockMovement.lineItems || []}
        />
      </div>
    </div>
  );
};

export default StockMovementOutboundShow;
