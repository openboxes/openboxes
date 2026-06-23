import mapToFormSelectOption from 'utils/mapToFormSelectOption';

// Temporary static autofill options for the putaway Location column header.
// Nothing is persisted yet. selecting an option does not fill or save anything.
const receivingLocationOptions = [
  { id: 'PREFERRED_BIN', name: 'Preferred bin' },
  { id: 'FILL_DOWN_FROM_TOP_ROW', name: 'Fill down from top row' },
  { id: 'RECEIVING_ROW', name: 'Receiving row' },
].map((option) => mapToFormSelectOption(option));

export default receivingLocationOptions;
