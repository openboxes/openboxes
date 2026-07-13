import mapToFormSelectOption from 'utils/mapToFormSelectOption';

export const LocationAutofillOption = {
  PREFERRED_BIN: 'PREFERRED_BIN',
  FILL_DOWN_FROM_TOP_ROW: 'FILL_DOWN_FROM_TOP_ROW',
  RECEIVING_BIN: 'RECEIVING_BIN',
};

const receivingLocationOptions = (translate) => [
  {
    id: LocationAutofillOption.PREFERRED_BIN,
    name: translate('react.receiving.autofillLocation.preferredBin.label', 'Preferred bin'),
  },
  {
    id: LocationAutofillOption.FILL_DOWN_FROM_TOP_ROW,
    name: translate('react.receiving.autofillLocation.fillDownFromTopRow.label', 'Fill down from top row'),
  },
  {
    id: LocationAutofillOption.RECEIVING_BIN,
    name: translate('react.receiving.autofillLocation.receivingBin.label', 'Receiving bin'),
  },
].map((option) => mapToFormSelectOption(option));

export default receivingLocationOptions;
