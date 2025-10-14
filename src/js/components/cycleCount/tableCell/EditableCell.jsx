import React from 'react';

import cycleCountColumn from 'consts/cycleCountColumn';

import BinLocationCell from './BinLocationCell';
import CommentCell from './CommentCell';
import ExpirationDateCell from './ExpirationDateCell';
import LotNumberCell from './LotNumberCell';
import QuantityCell from './QuantityCell';

const CellComponents = {
  [cycleCountColumn.BIN_LOCATION]: BinLocationCell,
  [cycleCountColumn.LOT_NUMBER]: LotNumberCell,
  [cycleCountColumn.EXPIRATION_DATE]: ExpirationDateCell,
  [cycleCountColumn.QUANTITY_COUNTED]: QuantityCell,
  [cycleCountColumn.COMMENT]: CommentCell,
};

const EditableCell = ({ row, column, table }) => {
  const columnPath = column.id.replaceAll('_', '.');
  const Component = CellComponents[columnPath] ?? CommentCell;

  const meta = table.options.meta || {};
  const { index } = row;
  const value = meta.tableData?.[index]?.[columnPath];

  return (
    <Component
      value={value}
      row={row}
      column={column}
      table={table}
      columnPath={columnPath}
      {...meta}
    />
  );
};

export default EditableCell;
