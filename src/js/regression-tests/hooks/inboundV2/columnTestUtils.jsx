// Shared helpers for the inbound column hook tests: plain stand-ins for the table cells
// (props exposed as data attributes) and a tiny table that renders column definitions
// through tanstack like DataTable does.
import React from 'react';

import { flexRender, getCoreRowModel, useReactTable } from '@tanstack/react-table';

export const MockTableCell = ({
  children, link, tooltipLabel, className,
}) => (
  <div
    data-testid="table-cell"
    data-link={link || undefined}
    data-tooltip={tooltipLabel ?? undefined}
    className={className}
  >
    {children}
  </div>
);

export const MockTableHeaderCell = ({ children, required, tooltipLabel }) => (
  <div data-testid="header-cell" data-required={required ? 'true' : undefined} data-tooltip={tooltipLabel}>
    {children}
  </div>
);

export const ColumnsTable = ({ columns, data }) => {
  const table = useReactTable({ columns, data, getCoreRowModel: getCoreRowModel() });
  return (
    <div>
      {table.getHeaderGroups().map((group) => (
        <div key={group.id} data-testid="header-row">
          {group.headers.map((header) => (
            <div key={header.id} data-testid={`header-${header.column.id}`}>
              {flexRender(header.column.columnDef.header, header.getContext())}
            </div>
          ))}
        </div>
      ))}
      {table.getRowModel().rows.map((row) => (
        <div key={row.id} data-testid={`row-${row.index}`}>
          {row.getVisibleCells().map((cell) => (
            <div key={cell.id} data-testid={`cell-${row.index}-${cell.column.id}`}>
              {flexRender(cell.column.columnDef.cell, cell.getContext())}
            </div>
          ))}
        </div>
      ))}
    </div>
  );
};
