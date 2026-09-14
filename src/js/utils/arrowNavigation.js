import navigationKey from 'consts/navigationKey';

// `DataTableBody` puts this on the cells of a table or a column that asked for the navigation.
const ARROW_NAVIGATION_ATTRIBUTE = 'data-arrow-navigation';

export const CELL_SELECTOR = `[${ARROW_NAVIGATION_ATTRIBUTE}]`;

// The field inside a marked cell that a move can land on. A hidden input cannot take the focus and
// a disabled one is meant to be skipped.
export const FIELD_SELECTOR = 'input:not([type="hidden"]):enabled, textarea:enabled';

// The class react-datepicker puts on its input while the calendar is open. The name is about its
// own outside-click handling, but it lands there exactly when the calendar is open.
const OPEN_CALENDAR_CLASS = 'react-datepicker-ignore-onclickoutside';

// Marks a cell the arrow keys can move into.
export const arrowNavigationProps = (enabled) => ({
  [ARROW_NAVIGATION_ATTRIBUTE]: enabled || undefined,
});

export const getColumnId = (cell) => cell.closest('[data-column-id]')?.dataset.columnId;

// Losing the focus does not close the calendar, so the field being left is asked to close it.
export const closeCalendarIfOpen = (field) => {
  if (field.classList.contains(OPEN_CALENDAR_CLASS)) {
    // Tab is the only key it closes on without sending the focus back, and its handler sits on
    // the input, so the key goes in as an event.
    field.dispatchEvent(new KeyboardEvent('keydown', { key: navigationKey.TAB, bubbles: true }));
  }
};
