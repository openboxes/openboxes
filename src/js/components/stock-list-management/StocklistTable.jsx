import React from 'react';
import PropTypes from 'prop-types';
import ReactTable from 'react-table';

import 'react-table/react-table.css';

const COLUMNS = [
  {
    Header: 'Stocklist Name',
    accessor: 'name',
  },
  {
    Header: 'Manager',
    accessor: 'manager.name',
  },
  {
    Header: 'Replenishment period',
    accessor: 'replenishmentPeriod',
  },
  {
    Header: 'Maximum  Quantity',
    accessor: 'maxQuantity',
  },
  {
    Header: 'Unit of measure',
    accessor: 'uom',
  },
];

const StocklistTable = ({ data }) => (
  <div>
    {
      data && !!data.length &&
      <ReactTable
        data={data}
        columns={COLUMNS}
        showPagination={false}
        minRows={0}
      />
    }
  </div>
);

export default StocklistTable;

StocklistTable.propTypes = {
  data: PropTypes.arrayOf(PropTypes.shape({})),
};

StocklistTable.defaultProps = {
  data: [],
};
