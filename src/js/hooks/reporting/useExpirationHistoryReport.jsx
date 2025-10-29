import React, { useMemo } from 'react';

import { createColumnHelper } from '@tanstack/react-table';

import TableHeaderCell from 'components/DataTable/TableHeaderCell';

const useExpirationHistoryReport = () => {
  const columnHelper = createColumnHelper();

  const columns = useMemo(() => [
    columnHelper.accessor('transactionId', {
      header: () => (
        <TableHeaderCell columnId="transactionId">
          Transaction Id
        </TableHeaderCell>
      ),
      cell: () => (
        <div className="rt-td pb-0 d-flex align-items-start">
          1
        </div>
      ),
    }),
    columnHelper.accessor('transactionDate', {
      header: () => (
        <TableHeaderCell columnId="transactionDate">
          Transaction Date
        </TableHeaderCell>
      ),
      cell: () => (
        <div className="rt-td pb-0 d-flex align-items-start">
          1
        </div>
      ),
    }), columnHelper.accessor('code', {
      header: () => (
        <TableHeaderCell columnId="code">
          Code
        </TableHeaderCell>
      ),
      cell: () => (
        <div className="rt-td pb-0 d-flex align-items-start">
          1
        </div>
      ),
    }), columnHelper.accessor('productName', {
      header: () => (
        <TableHeaderCell columnId="productName">
          Product Name
        </TableHeaderCell>
      ),
      cell: () => (
        <div className="rt-td pb-0 d-flex align-items-start">
          Category
        </div>
      ),
    }),
    columnHelper.accessor('category', {
      header: () => (
        <TableHeaderCell columnId="category">
          Category
        </TableHeaderCell>
      ),
      cell: () => (
        <div className="rt-td pb-0 d-flex align-items-start">
          1
        </div>
      ),
    }),
    columnHelper.accessor('lotNumber', {
      header: () => (
        <TableHeaderCell columnId="lotNumber">
          Lot Number
        </TableHeaderCell>
      ),
      cell: () => (
        <div className="rt-td pb-0 d-flex align-items-start">
          1
        </div>
      ),
    }), columnHelper.accessor('expirationDate', {
      header: () => (
        <TableHeaderCell columnId="expirationDate">
          Expiration Date
        </TableHeaderCell>
      ),
      cell: () => (
        <div className="rt-td pb-0 d-flex align-items-start">
          1
        </div>
      ),
    }), columnHelper.accessor('quantityLostToExpiry', {
      header: () => (
        <TableHeaderCell columnId="quantityLostToExpiry">
          Quantity Lost to Expiry
        </TableHeaderCell>
      ),
      cell: () => (
        <div className="rt-td pb-0 d-flex align-items-start">
          1
        </div>
      ),
    }), columnHelper.accessor('unitPrice', {
      header: () => (
        <TableHeaderCell columnId="unitPrice">
          Unit Price
        </TableHeaderCell>
      ),
      cell: () => (
        <div className="rt-td pb-0 d-flex align-items-start">
          1
        </div>
      ),
    }), columnHelper.accessor('valueLostToExpiry', {
      header: () => (
        <TableHeaderCell columnId="valueLostToExpiry">
          Value Lost to Expiry
        </TableHeaderCell>
      ),
      cell: () => (
        <div className="rt-td pb-0 d-flex align-items-start">
          1
        </div>
      ),
    }),
  ], []);

  return {
    tableData: { totalCount: 0, data: [] },
    columns,
    loading: false,
    emptyTableMessage: 'No data available',
  };
};

export default useExpirationHistoryReport;
