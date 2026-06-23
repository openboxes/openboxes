import React, { useMemo } from 'react';

import PropTypes from 'prop-types';

import DataTable from 'components/DataTable/v2/DataTable';

import 'components/receivingV2/receiving.scss';

const ReceivingTable = ({
  lineItemsState, columns, loading, updateLineItem,
}) => {
  // Keep `meta` stable so it only changes when the entities map or
  // the update function change. Combined with the memoized cells, a single line item update
  // re-renders just that row instead of the whole table.
  const meta = useMemo(
    () => ({ entities: lineItemsState.entities, updateLineItem }),
    [lineItemsState.entities, updateLineItem],
  );

  return (
    <div className="receiving-table">
      <DataTable
        columns={columns}
        data={lineItemsState.ids}
        totalCount={lineItemsState.ids.length}
        meta={meta}
        disablePagination
        loading={loading}
        loadingMessage={{
          id: 'react.default.loading.label',
          defaultMessage: 'Loading...',
        }}
        emptyTableMessage={{
          id: 'react.receiving.emptyTable.label',
          defaultMessage: 'No items to receive',
        }}
      />
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
};

export default ReceivingTable;
