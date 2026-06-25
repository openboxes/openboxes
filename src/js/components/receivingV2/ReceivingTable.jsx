import React, { useMemo } from 'react';

import PropTypes from 'prop-types';

import DataTable from 'components/DataTable/v2/DataTable';
import CommentModal from 'components/modals/CommentModal';

import 'components/receivingV2/receiving.scss';

const ReceivingTable = ({
  lineItemsState, columns, loading, updateLineItem, commentModal,
}) => {
  const {
    isOpen: isCommentModalOpen,
    openModal: openCommentModal,
    closeModal: closeCommentModal,
  } = commentModal;

  // Keep `meta` stable so it only changes when the entities map or
  // the update function change. Combined with the memoized cells, a single line item update
  // re-renders just that row instead of the whole table.
  const meta = useMemo(
    () => ({
      entities: lineItemsState.entities,
      updateLineItem,
      onOpenCommentModal: openCommentModal,
    }),
    [lineItemsState.entities, updateLineItem, openCommentModal],
  );

  // Separators pass through without meta. Meta is only used to disable (grey out)
  // fully received rows, and separators don't need disabling.
  const data = useMemo(
    () => lineItemsState.ids.map((entry) => {
      if (entry.isSeparator) {
        return entry;
      }
      return {
        id: entry,
        meta: {
          isRowDisabled: lineItemsState.entities[entry]?.isFullyReceived,
          label: 'react.receiving.fullyReceived.label',
          defaultMessage: 'This line has been fully received',
        },
      };
    }),
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
          estimateSize: 68,
          overscan: 10,
          // Rows vary in height (2-line product cell, separator rows), so let
          // the virtualizer measure each row instead of using a fixed height.
          customRowsHeight: true,
        }}
      />
      <CommentModal isOpen={isCommentModalOpen} onClose={closeCommentModal} />
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
  updateLineItem: PropTypes.func.isRequired,
  commentModal: PropTypes.shape({
    isOpen: PropTypes.bool.isRequired,
    openModal: PropTypes.func.isRequired,
    closeModal: PropTypes.func.isRequired,
  }).isRequired,
};

export default ReceivingTable;
