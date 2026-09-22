// `DataTableBody` puts this on the cells of a table or a column that asked for the navigation.
export const CELL_SELECTOR = '[data-arrow-navigation="true"]';

// The field inside a marked cell that a move can land on. A hidden input cannot take the focus and
// a disabled one is meant to be skipped.
export const FIELD_SELECTOR = 'input:not([type="hidden"]):enabled, textarea:enabled';

export const getColumnId = (cell) => cell.closest('[data-column-id]')?.dataset.columnId;
