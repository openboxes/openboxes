import { useEffect, useRef } from 'react';

import navigationKey from 'consts/navigationKey';
import { CELL_SELECTOR, FIELD_SELECTOR, getColumnId } from 'utils/arrowNavigation';
import closeCalendarIfOpen from 'utils/datePickerUtils';

// A `vertical` move only sees the fields of its own column, `step` is the way it walks that list.
// Left and right see the whole table as one list, so they continue in the neighbouring row.
const MOVE_BY_KEY = {
  [navigationKey.ARROW_UP]: { vertical: true, step: -1 },
  [navigationKey.ARROW_DOWN]: { vertical: true, step: 1 },
  [navigationKey.ARROW_LEFT]: { vertical: false, step: -1 },
  [navigationKey.ARROW_RIGHT]: { vertical: false, step: 1 },
};

/**
 * Arrow key navigation between the fields of a table. One listener, and the focus moves through
 * the DOM, so there is no state to keep and nothing re-renders.
 *
 * `DataTable` mounts this and marks the cells, so a table only passes its `arrowNavigation`
 * config object. Calling the hook and attaching the ref it returns is for markup `DataTable` does
 * not render. A cell with no enabled field is skipped.
 *
 * TODO: selects and date fields are handled and should work, but the navigation has only been
 * used on a column of text fields. Verify them before turning it on for a column with one.
 *
 * @param onNavigatePastLastField called with the column when a forward move runs out of the last
 *        field of the table.
 * @param verticalOnly leaves left and right to the field, so they move the caret in it
 */
const useTableArrowNavigation = ({ onNavigatePastLastField, verticalOnly } = {}) => {
  const tableRef = useRef(null);

  // The fields a move can land on, in the order they are rendered in. Without a `columnId` every
  // marked cell of the table is in scope, and a cell with no field to give drops out.
  const getFields = (table, columnId) => [...table.querySelectorAll(CELL_SELECTOR)]
    .filter((cell) => !columnId || getColumnId(cell) === columnId)
    .map((cell) => cell.querySelector(FIELD_SELECTOR))
    .filter(Boolean);

  const isLastNavigableFieldOfTable = (table, field) => {
    const tableFields = getFields(table);
    return tableFields.indexOf(field) === tableFields.length - 1;
  };

  // Hands the focus over, `from` being the field the move leaves and `to` the one it enters.
  const moveFocusTo = (from, to) => {
    if (!to) {
      return;
    }
    // The date picker keeps its calendar open when it loses the focus, so it has to be told.
    closeCalendarIfOpen(from);
    to.focus();
    // Select current text, so the next thing typed replaces the value.
    to.select();
  };

  const handleArrowKey = ({ event, table }) => {
    const move = MOVE_BY_KEY[event.key];
    if (!move || (verticalOnly && !move.vertical)) {
      return;
    }

    // The marked cell the focus is standing in before the move.
    const cell = event.target.closest(CELL_SELECTOR);
    if (!cell) {
      return;
    }

    // The column the focus is in before the move.
    const columnId = getColumnId(cell);

    // Up and down stay in the column of the field, left and right walk the whole table.
    const columnScope = move.vertical ? columnId : null;
    const fields = getFields(table, columnScope);

    // Kept from the field, which would step its value, open its menu or move its caret.
    event.preventDefault();
    event.stopPropagation();

    const targetIndex = fields.indexOf(event.target) + move.step;
    // A move with nothing ahead calls `onNavigatePastLastField` only when it leaves the last field
    // of the whole table, not when it runs out of the fields of one column.
    const hasNavigatedPastLastField = !fields[targetIndex]
      && move.step > 0
      && onNavigatePastLastField
      && isLastNavigableFieldOfTable(table, event.target);

    if (hasNavigatedPastLastField) {
      onNavigatePastLastField(columnId);
    }

    const targetField = hasNavigatedPastLastField
      ? getFields(table, columnScope)[targetIndex]
      : fields[targetIndex];

    moveFocusTo(event.target, targetField);
  };

  useEffect(() => {
    const table = tableRef.current;
    if (!table) {
      return undefined;
    }

    const handleKeyDown = (event) => handleArrowKey({ event, table });

    table.addEventListener('keydown', handleKeyDown);
    return () => table.removeEventListener('keydown', handleKeyDown);
  }, [onNavigatePastLastField, verticalOnly]);

  return tableRef;
};

export default useTableArrowNavigation;
