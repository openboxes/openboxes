import navigationKey from 'consts/navigationKey';

// The class react-datepicker puts on its input while the calendar is open. The name is about its
// own outside-click handling, but it lands there exactly when the calendar is open.
const OPEN_CALENDAR_CLASS = 'react-datepicker-ignore-onclickoutside';

// Losing the focus does not close the calendar, so the field being left is asked to close it.
const closeCalendarIfOpen = (field) => {
  if (field.classList.contains(OPEN_CALENDAR_CLASS)) {
    // Tab is the only key it closes on without sending the focus back, and its handler sits on
    // the input, so the key goes in as an event.
    field.dispatchEvent(new KeyboardEvent('keydown', { key: navigationKey.TAB, bubbles: true }));
  }
};

export default closeCalendarIfOpen;
