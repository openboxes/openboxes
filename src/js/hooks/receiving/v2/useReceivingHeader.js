import { useEffect, useMemo } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { getDefaultTranslationsFetched, getReceivingHeaderInfo } from 'selectors';

import { updateReceivingHeader } from 'actions';
import stockMovementApi from 'api/services/StockMovementApi';
import useTranslate from 'hooks/useTranslate';

/**
 * Fetches the shipment that is being received, builds the colored, delimited info segments
 * (shipment number, origin, destination, ship date, description) and stores them in redux
 * together with the isShipmentFromPurchaseOrder flag, so the header and the table can read
 * the slice they need without prop drilling.
 */
const useReceivingHeader = () => {
  const { shipmentId } = useParams();
  const translate = useTranslate();
  const dispatch = useDispatch();
  const info = useSelector(getReceivingHeaderInfo);
  const translationsFetched = useSelector(getDefaultTranslationsFetched);

  const buildHeaderInfo = useMemo(() => (shipment) => [
    {
      text: shipment.identifier,
      color: '#000000',
      delimeter: ' - ',
    },
    {
      text: shipment.origin?.name,
      color: '#004d40',
      // Guard against translate returning undefined before translations load.
      delimeter: ` ${translate('react.default.to.label', 'to') || 'to'} `,
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
  ].filter((segment) => segment.text), [translate]);

  const loadShipment = async () => {
    const { data: { data } } = await stockMovementApi.getStockMovementById(shipmentId);
    dispatch(updateReceivingHeader(buildHeaderInfo(data), Boolean(data?.isFromOrder)));
  };

  useEffect(() => {
    if (!shipmentId || !translationsFetched) {
      return;
    }

    loadShipment();
  }, [shipmentId, translationsFetched]);

  return { info };
};

export default useReceivingHeader;
