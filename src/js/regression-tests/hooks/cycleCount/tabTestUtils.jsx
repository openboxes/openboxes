// Shared helpers for the cycle count tab hook tests: lightweight stand-ins for the table
// components (so cells render plain DOM with their props exposed as data attributes) and
// helpers that render a tanstack column's header and cell the way DataTable would.
import React from 'react';

// eslint-disable-next-line import/no-extraneous-dependencies
import { render } from '@testing-library/react';
import _ from 'lodash';

export const MockTableCell = ({
  children, link, tooltipLabel, className,
}) => (
  <div
    data-testid="table-cell"
    data-link={link || undefined}
    data-tooltip={tooltipLabel}
    className={className}
  >
    {children}
  </div>
);

export const MockTableHeaderCell = ({ children, columnId, sortable }) => (
  <div data-testid="header-cell" data-column={columnId} data-sortable={sortable ? 'true' : undefined}>
    {children}
  </div>
);

export const MockCheckbox = ({
  value, onChange, onClick, disabled, className,
}) => (
  <input
    type="checkbox"
    data-testid="checkbox"
    checked={!!value}
    disabled={disabled}
    className={className}
    onChange={onChange || (() => {})}
    onClick={onClick}
  />
);

export const MockStatusIndicator = ({ variant, status }) => (
  <span data-testid="status" data-variant={variant}>{status}</span>
);

export const MockBadge = ({ label, variant }) => (
  <span data-testid="badge" data-variant={variant}>{label}</span>
);

export const MockValueIndicator = ({ value, variant, className }) => (
  <span data-testid="value-indicator" data-variant={variant} className={className}>{value}</span>
);

export const MockSpinner = () => <span data-testid="spinner" />;

// jsdom under Node 14 has no crypto.randomUUID, which bin location cells use for keys.
export const installRandomUUID = () => {
  let counter = 0;
  if (!global.crypto) {
    global.crypto = {};
  }
  global.crypto.randomUUID = () => {
    counter += 1;
    return `uuid-${counter}`;
  };
};

export const getColumnId = (column) => column.id ?? column.accessorKey;

export const findColumn = (columns, id) => columns.find((column) => getColumnId(column) === id);

export const getColumnValue = (column, original) => (column.accessorFn
  ? column.accessorFn(original, 0)
  : _.get(original, column.accessorKey));

// Cells may call hooks (selectRow uses useCallback), so they render inside a component.
export const renderCell = (column, original) => {
  const Cell = () => column.cell({
    getValue: () => getColumnValue(column, original),
    row: { original },
  }) ?? null;
  return render(<Cell />);
};

export const renderHeader = (column) => {
  const Header = () => column.header();
  return render(<Header />);
};
