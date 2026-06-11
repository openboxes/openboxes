import React from 'react';

import PropTypes from 'prop-types';

import DataTable from 'components/DataTable/v2/DataTable';

import 'components/receiving/receiving.scss';

const ReceivingTable = ({ lineItems, columns, loading }) => (
  <div className="receiving-table">
    <DataTable
      columns={columns}
      data={lineItems}
      totalCount={lineItems.length}
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

ReceivingTable.propTypes = {
  lineItems: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  columns: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  loading: PropTypes.bool.isRequired,
};

export default ReceivingTable;
