import { useCallback } from 'react';

import { useDispatch } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import receivingApi from 'api/services/ReceivingApi';

/**
 * Save action for the receiving comment popover. A receipt item holds at most one comment, so the
 * backend splits creating and editing into separate endpoints - `isUpdate` selects between them.
 * On success the comment is folded back into the row's normalized state (without marking it dirty,
 * so it never gets picked up by the receiving quantities batch save).
 */
const useSaveComment = ({ updateLineItemComment, onClose }) => {
  const dispatch = useDispatch();

  const saveComment = useCallback(async ({
    receiptItemId, rowId, comment, isUpdate,
  }) => {
    if (!receiptItemId) {
      return;
    }

    dispatch(showSpinner());
    try {
      const { data: { data } } = isUpdate
        ? await receivingApi.updateReceiptItemComment(receiptItemId, { comment })
        : await receivingApi.createReceiptItemComment(receiptItemId, { comment });
      updateLineItemComment(rowId, data?.comment);
      onClose();
    } finally {
      dispatch(hideSpinner());
    }
  }, [dispatch, updateLineItemComment, onClose]);

  return { saveComment };
};

export default useSaveComment;
