import { useCallback } from 'react';

import { useDispatch } from 'react-redux';
import { useParams } from 'react-router-dom';

import { hideSpinner, showSpinner } from 'actions';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import confirmExitWithUnsavedRows from 'utils/receiving/confirmExitWithUnsavedRows';

const useReceivingSaveAction = ({ flush }) => {
  const { shipmentId } = useParams();
  const dispatch = useDispatch();

  // Autosave persists edits continuously, so exiting only needs to flush whatever is still unsaved.
  const onSaveAndExit = useCallback(async () => {
    dispatch(showSpinner());
    let hasUnsavedRows = false;
    try {
      await flush();
    } catch {
      hasUnsavedRows = true;
    } finally {
      dispatch(hideSpinner());
    }
    // If the flush failed (some rows could not be saved, or are invalid), prompt the user to
    // decide if they still want to exit and lose those updates.
    if (hasUnsavedRows && !(await confirmExitWithUnsavedRows())) {
      return;
    }
    window.location = STOCK_MOVEMENT_URL.show(shipmentId);
  }, [flush, shipmentId]);

  return {
    onSaveAndExit,
  };
};

export default useReceivingSaveAction;
