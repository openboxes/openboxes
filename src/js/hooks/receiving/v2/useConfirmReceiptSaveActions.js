import { useCallback } from 'react';

import { useDispatch } from 'react-redux';
import { useParams } from 'react-router-dom';

import { hideSpinner, removeReceivingPutawayEnabled, showSpinner } from 'actions';
import receivingApi from 'api/services/ReceivingApi';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import buildReceiptCompletePayload from 'utils/receiving/buildReceiptCompletePayload';

/**
 * Exit actions of the check step: leaving with the receipt still pending, and completing it.
 * Nothing on this step is edited in place (quantities and locations are saved on the receiving
 * step, comments on their own endpoint), so leaving only navigates away.
 */
const useConfirmReceiptSaveActions = ({ receiptIdRef, itemsToComplete }) => {
  const dispatch = useDispatch();
  const { shipmentId } = useParams();

  const exitToShipment = () => {
    window.location = STOCK_MOVEMENT_URL.show(shipmentId);
  };

  const onSaveAndExit = useCallback(exitToShipment, [shipmentId]);

  // Runs as the react-hook-form submit handler, so it only gets here with the delivery date
  // filled in. A failed request keeps the user on the page (the api client surfaces the error)
  // so the cancel remaining selection is not lost.
  const onCompleteReceipt = useCallback(async ({ dateDelivered }) => {
    const receiptId = receiptIdRef.current;
    if (!receiptId) {
      return;
    }
    dispatch(showSpinner());
    try {
      await receivingApi.completeReceipt(
        receiptId,
        buildReceiptCompletePayload({ dateDelivered, itemsToComplete }),
      );
    } catch {
      // The api client already received the error. `Return` here keeps react-hook-form's
      // submit handler from rejecting, which would leave its isSubmitting flag stuck on true.
      return;
    } finally {
      dispatch(hideSpinner());
    }
    // The putaway toggle is remembered per receipt while receiving; a completed receipt has no
    // use for it anymore.
    dispatch(removeReceivingPutawayEnabled(receiptId));
    exitToShipment();
  }, [dispatch, itemsToComplete, shipmentId]);

  return { onSaveAndExit, onCompleteReceipt };
};

export default useConfirmReceiptSaveActions;
