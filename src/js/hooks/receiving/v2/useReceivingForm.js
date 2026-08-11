import {
  useCallback, useEffect, useRef,
} from 'react';

import { useDispatch, useSelector } from 'react-redux';
import {
  getReceivingBin,
  getReceivingBinLocations,
  getReceivingPutawayEnabled,
  getReceivingView,
} from 'selectors';

import { updateReceivingPutawayEnabled, updateReceivingView } from 'actions';
import useReceivingActions from 'hooks/receiving/v2/useReceivingActions';
import useReceivingBinLocations from 'hooks/receiving/v2/useReceivingBinLocations';
import useReceivingColumns from 'hooks/receiving/v2/useReceivingColumns';
import useReceivingFilters from 'hooks/receiving/v2/useReceivingFilters';
import useTableLocationAutofill from 'hooks/receiving/v2/useTableLocationAutofill';
import useTableSorting from 'hooks/useTableSorting';
import anyLineHasOtherBin from 'utils/receiving/anyLineHasOtherBin';

const useReceivingForm = () => {
  const dispatch = useDispatch();
  // The selected view is shared through redux, so the check step renders in the
  // view chosen here.
  const view = useSelector(getReceivingView);
  const setView = useCallback((newView) => dispatch(updateReceivingView(newView)), [dispatch]);
  const receivingBin = useSelector(getReceivingBin);
  const binLocations = useSelector(getReceivingBinLocations);
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
  const { visibleLineItemsState, updateFilterParams } = useReceivingFilters({ lineItemsState });
  const { onLocationAutofill } = useTableLocationAutofill({
    lineItemsState: visibleLineItemsState,
    updateLineItems,
  });
  const autofillVisibleQuantities = useCallback(
    () => autofillQuantities(visibleLineItemsState),
    [autofillQuantities, visibleLineItemsState],
  );
  // Auto-enable once when a line sits somewhere other than its default location, so the column is
  // visible: a bin other than the receiving bin, or any bin at all when there is no receiving bin.
  const putawayInitialized = useRef(false);
  useEffect(() => {
    if (putawayInitialized.current || !binLocations.length || !lineItemsState?.ids?.length) {
      return;
    }
    putawayInitialized.current = true;
    if (anyLineHasOtherBin(lineItemsState, receivingBin)) {
      setPutawayEnabled(true);
    }
  }, [lineItemsState, receivingBin, binLocations]);
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
    },
  };
};

export default useReceivingForm;
