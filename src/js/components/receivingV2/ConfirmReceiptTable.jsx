import React, { useMemo } from 'react';

import PropTypes from 'prop-types';

import DataTable from 'components/DataTable/v2/DataTable';
import CommentModal from 'components/modals/CommentModal';
import buildReceivingTableRows from 'utils/receiving/buildReceivingTableRows';

import 'components/receivingV2/receiving.scss';

const ConfirmReceiptTable = ({
  lineItemsState,
  columns,
  loading,
  commentModal,
  cancelRemaining,
  sort,
  order,
}) => {
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

  // Keep `meta` stable so it only changes when the entities map or the cancel remaining
  // selection change. Combined with the memoized cells, a single row update re-renders
  // just that row instead of the whole table.
  const meta = useMemo(
    () => ({
      entities: lineItemsState.entities,
      onOpenCommentModal,
      cancelRemainingIds: cancelRemaining.ids,
      onToggleCancelRemaining: cancelRemaining.toggle,
    }),
    [
      lineItemsState.entities,
      onOpenCommentModal,
      cancelRemaining.ids,
      cancelRemaining.toggle,
    ],
  );

  const data = useMemo(() => buildReceivingTableRows(lineItemsState), [lineItemsState]);

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
    </div>
  );
};

ConfirmReceiptTable.propTypes = {
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
  commentModal: PropTypes.shape({
    isOpen: PropTypes.bool.isRequired,
    itemId: PropTypes.string,
    anchor: PropTypes.shape({
      top: PropTypes.number,
      right: PropTypes.number,
    }),
    onOpenCommentModal: PropTypes.func.isRequired,
    closeModal: PropTypes.func.isRequired,
    saveComment: PropTypes.func.isRequired,
  }).isRequired,
  cancelRemaining: PropTypes.shape({
    ids: PropTypes.instanceOf(Set).isRequired,
    toggle: PropTypes.func.isRequired,
  }).isRequired,
  sort: PropTypes.string,
  order: PropTypes.string,
};

ConfirmReceiptTable.defaultProps = {
  sort: null,
  order: null,
};

export default ConfirmReceiptTable;
