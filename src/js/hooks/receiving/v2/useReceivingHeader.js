import { useEffect, useMemo, useState } from 'react';

import { useParams } from 'react-router-dom';

import stockMovementApi from 'api/services/StockMovementApi';
import useTranslate from 'hooks/useTranslate';

/**
 * Fetches the shipment that is being received and builds the colored, delimited
 * info segments (shipment number, origin, destination, ship date, description)
 */
const useReceivingHeader = () => {
  const { shipmentId } = useParams();
  const translate = useTranslate();
  const [shipment, setShipment] = useState(null);

  const loadShipment = async () => {
    const { data: { data } } = await stockMovementApi.getStockMovementById(shipmentId);
    setShipment(data);
  };

  useEffect(() => {
    if (!shipmentId) {
      return;
    }

    loadShipment();
  }, [shipmentId]);

  const info = useMemo(() => {
    if (!shipment) {
      return [];
    }

    return [
      {
        text: shipment.identifier,
        color: '#000000',
        delimeter: ' - ',
      },
      {
        text: shipment.origin?.name,
        color: '#004d40',
        delimeter: ` ${translate('react.default.to.label', 'to')} `,
      },
      {
        text: shipment.destination?.name,
        color: '#01579b',
        delimeter: ', ',
      },
      {
        text: shipment.dateShipped,
        color: '#4a148c',
        delimeter: ', ',
        isDate: true,
      },
      {
        text: shipment.description,
        color: '#770838',
        delimeter: '',
      },
    ].filter((segment) => segment.text);
  }, [shipment, translate]);

  return { info };
};

export default useReceivingHeader;
