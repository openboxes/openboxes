// `DataTableBody` puts this on the cells of a table or a column that asked for the navigation.
const ARROW_NAVIGATION_ATTRIBUTE = 'data-arrow-navigation';

export const CELL_SELECTOR = `[${ARROW_NAVIGATION_ATTRIBUTE}]`;

// The field inside a marked cell that a move can land on. A hidden input cannot take the focus and
// a disabled one is meant to be skipped.
export const FIELD_SELECTOR = 'input:not([type="hidden"]):enabled, textarea:enabled';

// Marks a cell the arrow keys can move into.
export const arrowNavigationProps = (enabled) => ({
  [ARROW_NAVIGATION_ATTRIBUTE]: enabled || undefined,
});

export const getColumnId = (cell) => cell.closest('[data-column-id]')?.dataset.columnId;
