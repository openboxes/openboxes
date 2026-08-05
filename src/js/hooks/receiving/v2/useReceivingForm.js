import {
  useCallback, useEffect, useRef,
} from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { getReceivingPutawayEnabled, getReceivingView } from 'selectors';

import { updateReceivingPutawayEnabled, updateReceivingView } from 'actions';
import useReceivingActions from 'hooks/receiving/v2/useReceivingActions';
import useReceivingBinLocations from 'hooks/receiving/v2/useReceivingBinLocations';
import useReceivingColumns from 'hooks/receiving/v2/useReceivingColumns';
import useReceivingFilters from 'hooks/receiving/v2/useReceivingFilters';
import useTableLocationAutofill from 'hooks/receiving/v2/useTableLocationAutofill';
import useTableSorting from 'hooks/useTableSorting';

const useReceivingForm = () => {
  const dispatch = useDispatch();
  // The selected view is shared through redux, so the check step renders in the
  // view chosen here.
  const view = useSelector(getReceivingView);
  const setView = useCallback((newView) => dispatch(updateReceivingView(newView)), [dispatch]);
  const {
    sortableProps, sort, order, resetSort,
  } = useTableSorting();
  const {
    loading,
    receiptId,
    lineItemsState,
    updateLineItem,
    updateLineItems,
    updateLineItemComment,
    autofillQuantities,
    removeSplitItem,
    loadReceipt,
    onSaveAndExit,
    flush,
    autosaveStatus,
  } = useReceivingActions({ view, sort, sortOrder: order });
  // The putaway toggle is remembered per receiving in redux, keyed by receipt id.
  const putawayEnabled = useSelector((state) => getReceivingPutawayEnabled(state, receiptId));
  const setPutawayEnabled = useCallback((enabled) => {
    if (!receiptId) {
      return;
    }
    dispatch(updateReceivingPutawayEnabled(receiptId, enabled));
  }, [dispatch, receiptId]);
  useReceivingBinLocations();
  const {
    visibleLineItemsState,
    updateFilterParams,
    clearFilterParams,
  } = useReceivingFilters({ lineItemsState });
  const { onLocationAutofill } = useTableLocationAutofill({
    lineItemsState: visibleLineItemsState,
    updateLineItems,
  });
  const autofillVisibleQuantities = useCallback(
    () => autofillQuantities(visibleLineItemsState),
    [autofillQuantities, visibleLineItemsState],
  );
  // Auto-enable once when a reopened receipt has at least one row with a saved bin location,
  // so the column is visible.
  const putawayInitialized = useRef(false);
  useEffect(() => {
    if (putawayInitialized.current || !lineItemsState?.ids?.length) {
      return;
    }
    putawayInitialized.current = true;
    if (Object.values(lineItemsState.entities).some((item) => item?.binLocation)) {
      setPutawayEnabled(true);
    }
  }, [lineItemsState]);
  const { columns } = useReceivingColumns({
    view,
    putawayEnabled,
    sortableProps,
    sort,
    order,
  });

  return {
    view,
    setView,
    putawayEnabled,
    setPutawayEnabled,
    table: {
      lineItemsState: visibleLineItemsState,
      columns,
      sort,
      order,
    },
    actions: {
      loading,
      receiptId,
      updateLineItem,
      updateLineItemComment,
      autofillQuantities: autofillVisibleQuantities,
      removeSplitItem,
      loadReceipt,
      onSaveAndExit,
      flush,
      onLocationAutofill,
      autosaveStatus,
      resetSort,
      updateFilterParams,
      clearFilterParams,
    },
  };
};

export default useReceivingForm;
