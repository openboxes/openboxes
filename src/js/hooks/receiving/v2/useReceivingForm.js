import {
  useCallback, useEffect, useMemo, useRef,
} from 'react';

import { useDispatch, useSelector } from 'react-redux';
import {
  getHasBinLocationSupport,
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
import useReceivingNextValidation from 'hooks/receiving/v2/useReceivingNextValidation';
import useReceivingSort from 'hooks/receiving/v2/useReceivingSort';
import useTableLocationAutofill from 'hooks/receiving/v2/useTableLocationAutofill';
import getOptionalColumnsVisibility from 'utils/receiving/getOptionalColumnsVisibility';
import hasItemInDifferentBin from 'utils/receiving/hasItemInDifferentBin';

const useReceivingForm = () => {
  const dispatch = useDispatch();
  // The selected view is shared through redux, so the check step renders in the
  // view chosen here.
  const view = useSelector(getReceivingView);
  const setView = useCallback((newView) => dispatch(updateReceivingView(newView)), [dispatch]);
  const receivingBin = useSelector(getReceivingBin);
  const binLocations = useSelector(getReceivingBinLocations);
  const hasBinLocationSupport = useSelector(getHasBinLocationSupport);
  // The sorting is shared with the check step, so it survives moving on from here.
  const {
    sortableProps, sort, order, resetSort,
  } = useReceivingSort();
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
  useReceivingBinLocations({ receiptId });
  const {
    visibleLineItemsState,
    updateFilterParams,
    clearFilterParams,
  } = useReceivingFilters({ lineItemsState });

  const { isNextDisabled, validateBeforeNext } = useReceivingNextValidation({ lineItemsState });

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
    if (putawayInitialized.current
      || !binLocations.length
      || !lineItemsState?.ids?.length
      || !hasBinLocationSupport) {
      return;
    }
    putawayInitialized.current = true;
    if (hasItemInDifferentBin(lineItemsState, receivingBin)) {
      setPutawayEnabled(true);
    }
  }, [lineItemsState, receivingBin, binLocations]);
  // Optional columns are read from the full state, so filtering the table down to rows
  // without a lot or a recipient does not collapse their columns.
  const columnsVisibility = useMemo(
    () => getOptionalColumnsVisibility(lineItemsState),
    [lineItemsState],
  );
  const { columns } = useReceivingColumns({
    view,
    putawayEnabled,
    sortableProps,
    sort,
    order,
    ...columnsVisibility,
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
    next: {
      // Nothing is known about the lines until the receipt is loaded, so the transition waits
      // for it - otherwise the validation would run on an empty table.
      isNextDisabled: loading || isNextDisabled,
      validateBeforeNext,
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
