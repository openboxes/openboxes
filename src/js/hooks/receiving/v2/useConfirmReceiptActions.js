import {
  useCallback, useEffect, useRef, useState,
} from 'react';

import _ from 'lodash';
import { useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { getHasPartialReceivingSupport, getUsers } from 'selectors';

import receivingApi from 'api/services/ReceivingApi';
import { createNormalizedState } from 'utils/normalizationUtils';
import getReceiptSummaryParams from 'utils/receiving/getReceiptSummaryParams';
import omitBlankReceivingRows from 'utils/receiving/omitBlankReceivingRows';
import { transformReceiptSummary } from 'utils/receiving/receiptSummaryRows';

const useConfirmReceiptActions = ({ view, sort, sortOrder } = {}) => {
  const [loading, setLoading] = useState(false);
  const receiptIdRef = useRef(null);
  const [lineItemsState, setLineItemsState] = useState(createNormalizedState());
  const { shipmentId } = useParams();
  const users = useSelector(getUsers);
  const hasPartialReceivingSupport = useSelector(getHasPartialReceivingSupport);

  const loadSummary = async () => {
    setLoading(true);
    try {
      const { data: { data: summary } } = await receivingApi.getReceiptSummary(
        shipmentId,
        getReceiptSummaryParams({ view, sort, sortOrder }),
      );
      receiptIdRef.current = summary?.pendingReceiptId ?? null;
      const rows = transformReceiptSummary(summary, view, _.keyBy(users, 'id'));
      // With partial receiving the lines left blank are not part of this receipt, so they stay
      // out of the review.
      setLineItemsState(hasPartialReceivingSupport ? omitBlankReceivingRows(rows) : rows);
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
  }, [shipmentId, view, sort, sortOrder]);

  return {
    loading,
    receiptIdRef,
    lineItemsState,
    updateLineItemComment,
  };
};

export default useConfirmReceiptActions;
