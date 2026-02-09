/**
 * Maximum date that can be selected in DatePicker components.
 * The datePicker throws errors when we set a date later than this one.
 * We use 31st December 9999 as the upper bound for date selection.
 */
const MAX_DATE_PICKER_DATE = new Date(9999, 11, 31);

export default MAX_DATE_PICKER_DATE;
