import {
  useCallback, useEffect, useRef, useState,
} from 'react';

import _ from 'lodash';
import { useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { getUsers } from 'selectors';

import receivingApi from 'api/services/ReceivingApi';
import { createNormalizedState } from 'utils/normalizationUtils';
import {
  receiptGroupForView,
  transformReceiptSummary,
} from 'utils/receiving/receiptSummaryRows';

const useConfirmReceiptActions = (view) => {
  const [loading, setLoading] = useState(false);
  const receiptIdRef = useRef(null);
  const [lineItemsState, setLineItemsState] = useState(createNormalizedState());
  const { shipmentId } = useParams();
  const users = useSelector(getUsers);

  const loadSummary = async () => {
    setLoading(true);
    try {
      const { data: { data: summary } } = await receivingApi.getReceiptSummary(shipmentId, {
        group: receiptGroupForView(view),
      });
      receiptIdRef.current = summary?.pendingReceiptId ?? null;
      setLineItemsState(transformReceiptSummary(summary, view, _.keyBy(users, 'id')));
    } finally {
      setLoading(false);
    }
  };

  // Comments are persisted on their own endpoint, so this only mirrors the already-saved comment
  // into the local rows, keeping the popover prefill and the create-vs-update choice correct
  // without reloading the whole summary.
  const updateLineItemComment = useCallback((rowId, comment) => {
    setLineItemsState((state) => ({
      ...state,
      entities: {
        ...state.entities,
        [rowId]: { ...state.entities[rowId], comment },
      },
    }));
  }, []);

  useEffect(() => {
    if (!shipmentId) {
      return;
    }
    loadSummary();
  }, [shipmentId, view]);

  return {
    loading,
    receiptIdRef,
    lineItemsState,
    updateLineItemComment,
  };
};

export default useConfirmReceiptActions;
