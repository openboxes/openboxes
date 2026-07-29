import React, { useMemo } from 'react';

import PropTypes from 'prop-types';

import DataTable from 'components/DataTable/v2/DataTable';
import CommentModal from 'components/modals/CommentModal';
import EditLineItemModal from 'components/receivingV2/editModal/EditLineItemModal';
import ReceivingRowType from 'consts/receivingRowType';
import useCommentModal from 'hooks/receiving/v2/useCommentModal';
import useEditReceivingLineItemModal from 'hooks/receiving/v2/useEditReceivingLineItemModal';

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

  // Separators pass through without meta. Meta is only used to disable (grey out)
  // fully received rows, and separators don't need disabling.
  const data = useMemo(
    () => {
      const { entities, ids } = lineItemsState;

      const buildRow = (rowId) => ({
        id: rowId,
        meta: {
          isRowDisabled: entities[rowId]?.isCompleted,
          label: 'react.receiving.fullyReceived.label',
          defaultMessage: 'This line has been fully received',
        },
      });

      // Split item rows, rendered by TanStack as subRows of their toggle row.
      // Split items of the same product merge into one visual block.
      const buildSubRow = (splitItemId, splitItemIndex, splitItemIds) => {
        const nextSplitItem = entities[splitItemIds[splitItemIndex + 1]];
        return {
          id: splitItemId,
          mergeWithNextRow: Boolean(nextSplitItem && !nextSplitItem.isFirstSplitItem),
          isLastSubRow: splitItemIndex === splitItemIds.length - 1,
        };
      };

      return ids
        // Split item rows render as subRows of their toggle row, not at the top level.
        .filter((entry) => entities[entry]?.rowType !== ReceivingRowType.SPLIT_ITEM)
        .map((entry) => {
          if (entry.isSeparator) {
            return entry;
          }
          return {
            ...buildRow(entry),
            subRows: entities[entry]?.splitItemIds?.map(buildSubRow),
            // A replaced row is always followed by its toggle row and merges with it.
            mergeWithNextRow: entities[entry]?.rowType === ReceivingRowType.REPLACED,
          };
        });
    },
    [lineItemsState],
  );

  return (
    <div className="receiving-table">
      <DataTable
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
            isUpdate: Boolean(commentItem.comment),
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
};

ReceivingTable.defaultProps = {
  receiptId: null,
};

export default ReceivingTable;
