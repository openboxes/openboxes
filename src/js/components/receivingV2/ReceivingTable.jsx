import React, { useMemo } from 'react';

import PropTypes from 'prop-types';

import DataTable from 'components/DataTable/v2/DataTable';
import CommentModal from 'components/modals/CommentModal';
import EditLineItemModal from 'components/receivingV2/editModal/EditLineItemModal';
import useCommentModal from 'hooks/receiving/v2/useCommentModal';
import useEditReceivingLineItemModal from 'hooks/receiving/v2/useEditReceivingLineItemModal';
import buildReceivingTableRows from 'utils/receiving/buildReceivingTableRows';

import 'components/receivingV2/receiving.scss';

const ReceivingTable = ({
  lineItemsState,
  columns,
  loading,
  receiptId,
  updateLineItem,
  updateLineItemComment,
  removeSplitItem,
  loadReceipt,
  onLocationAutofill,
  sort,
  order,
}) => {
  const commentModal = useCommentModal({ updateLineItemComment });
  const {
    isOpen: isCommentModalOpen,
    itemId: commentItemId,
    anchor: commentAnchor,
    onOpenCommentModal,
    closeModal: closeCommentModal,
    saveComment,
  } = commentModal;

  // The comment is edited on the row's receipt item; the entity carries its id (to target the
  // endpoint) and the saved comment (to prefill the form and choose create vs update).
  const commentItem = commentItemId ? lineItemsState.entities[commentItemId] : null;

  const {
    isOpen: isEditModalOpen,
    itemId: editedItemId,
    openModal: openEditModal,
    closeModal: closeEditModal,
    getInitialEditModalLineItems,
  } = useEditReceivingLineItemModal(lineItemsState);

  // Keep `meta` stable so it only changes when the entities map or
  // the update function change. Combined with the memoized cells, a single line item update
  // re-renders just that row instead of the whole table.
  const meta = useMemo(
    () => ({
      entities: lineItemsState.entities,
      updateLineItem,
      removeSplitItem,
      onOpenCommentModal,
      onOpenEditModal: openEditModal,
      onLocationAutofill,
    }),
    [
      lineItemsState.entities,
      updateLineItem,
      onOpenCommentModal,
      openEditModal,
      removeSplitItem,
      onLocationAutofill,
    ],
  );

  const data = useMemo(
    () => buildReceivingTableRows(lineItemsState),
    [lineItemsState],
  );

  return (
    <div className="receiving-table receiving-table--striped">
      <DataTable
        // Force TanStack table remount whenever the sort changes so the initialState
        // (`expanded: true`) re-applies and every changes group is expanded again.
        key={`${sort ?? ''}-${order ?? ''}`}
        columns={columns}
        data={data}
        totalCount={data.length}
        meta={meta}
        disablePagination
        tableWithPinnedColumns
        loading={loading}
        loadingMessage={{
          id: 'react.default.loading.label',
          defaultMessage: 'Loading...',
        }}
        emptyTableMessage={{
          id: 'react.receiving.emptyTable.label',
          defaultMessage: 'No items to receive',
        }}
        virtualize={{
          enabled: true,
          minSize: 20,
          estimateSize: 72,
          overscan: 10,
          // Rows vary in height (2-line product cell, separator rows), so let
          // the virtualizer measure each row instead of using a fixed height.
          customRowsHeight: true,
        }}
        getSubRows={(row) => row.subRows}
        defaultExpandedSubRows
      />
      {isCommentModalOpen && commentItem && (
        <CommentModal
          onClose={closeCommentModal}
          anchor={commentAnchor}
          initialValue={commentItem.comment ?? ''}
          onSave={(comment) => saveComment({
            receiptItemId: commentItem.receiptItemId,
            rowId: commentItemId,
            comment,
            isUpdate: commentItem.comment !== null,
          })}
        />
      )}
      {isEditModalOpen && (
        <EditLineItemModal
          onClose={closeEditModal}
          lineItem={lineItemsState.entities[editedItemId]}
          initialLineItems={getInitialEditModalLineItems(editedItemId)}
          receiptId={receiptId}
          loadReceipt={loadReceipt}
        />
      )}
    </div>
  );
};

ReceivingTable.propTypes = {
  lineItemsState: PropTypes.shape({
    entities: PropTypes.shape({}),
    ids: PropTypes.arrayOf(PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number,
      // Type for separator rows ({ isSeparator, name })
      PropTypes.shape({}),
    ])),
  }).isRequired,
  columns: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  loading: PropTypes.bool.isRequired,
  receiptId: PropTypes.string,
  updateLineItem: PropTypes.func.isRequired,
  updateLineItemComment: PropTypes.func.isRequired,
  removeSplitItem: PropTypes.func.isRequired,
  loadReceipt: PropTypes.func.isRequired,
  onLocationAutofill: PropTypes.func.isRequired,
  sort: PropTypes.string,
  order: PropTypes.string,
};

ReceivingTable.defaultProps = {
  receiptId: null,
  sort: null,
  order: null,
};

export default ReceivingTable;
