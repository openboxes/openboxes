import { useCallback } from 'react';

import { useDispatch } from 'react-redux';
import { useParams } from 'react-router-dom';

import { hideSpinner, showSpinner } from 'actions';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';

const useReceivingSaveAction = ({ flush }) => {
  const { shipmentId } = useParams();
  const dispatch = useDispatch();

  // Autosave persists edits continuously, so exiting only needs to flush whatever is
  // still unsaved. When the flush fails (some rows could not be saved even after a
  // retry), we stay on the page - the autosave indicator surfaces the error.
  const onSaveAndExit = useCallback(async () => {
    dispatch(showSpinner());
    try {
      await flush();
    } finally {
      dispatch(hideSpinner());
    }
    window.location = STOCK_MOVEMENT_URL.show(shipmentId);
  }, [flush, shipmentId]);

  return {
    onSaveAndExit,
  };
};

export default useReceivingSaveAction;
